package it.w4bo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.github.cdimascio.dotenv.dotenv
import org.json.JSONArray
import org.json.JSONObject
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.*

class Day {
    val weekDay: String = ""
    val bin: String = ""
    val where: String = ""
    override fun toString(): String {
        return "$weekDay $bin $where"
    }
}

class Doodle {
    var week: List<Day> = mutableListOf()
    var from: String = ""
    var to: String = ""
    override fun toString(): String {
        return "{from: $from, to: $to, weeks: [${week.map { it.toString() }.reduce { a, b -> "$a, $b" }}]}"
    }
}

class Doodles {
    var doodles: List<Doodle> = mutableListOf()
    override fun toString(): String {
        return doodles.map { it.toString() }.reduce { a, b -> "$a\n$b" }
    }
}

fun getResourceAsText(path: String) = object {}.javaClass.getResource(path)

fun getDoodles(): Doodles {
    val mapper = ObjectMapper(YAMLFactory())
    return mapper.readValue(getResourceAsText("/turni.yml"), Doodles::class.java)
}

fun getCalendarWithoutTime(date: Date): Calendar {
    val calendar: Calendar = GregorianCalendar()
    calendar.time = date
    calendar[Calendar.HOUR] = 0
    calendar[Calendar.HOUR_OF_DAY] = 0
    calendar[Calendar.MINUTE] = 0
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MILLISECOND] = 0
    return calendar
}

fun getConn(): Connection {
    val dotenv = dotenv()
    // Class.forName("com.mysql.jdbc.Driver").newInstance()
    // return DriverManager.getConnection(
    //     "jdbc:mysql://${dotenv["MYSQL_IP"]}:${dotenv["MYSQL_PORT"]}/${dotenv["MYSQL_DB"]}",
    //     dotenv["MYSQL_USER"],
    //     dotenv["MYSQL_PWD"]
    // )
    Class.forName("org.postgresql.Driver").newInstance()
    return DriverManager.getConnection(
        "jdbc:postgresql://${dotenv["POSTGRES_IP"]}:${dotenv["POSTGRES_PORT"]}/${dotenv["POSTGRES_DB"]}",
        dotenv["POSTGRES_USER"],
        dotenv["POSTGRES_PWD"]
    )
}

fun updateDoodle(turni: JSONArray) {
    val con = getConn()
    val prepStmt = con.prepareStatement("""INSERT INTO userindoodle (checked, id, slotbin, slotwhere, slotdate) values (?, ?, ?, ?, ?) ON CONFLICT (id, slotdate, slotbin, slotwhere) DO UPDATE SET checked = EXCLUDED.checked""")
    // val prepStmt = con.prepareStatement("""UPDATE userindoodle SET checked=? WHERE id=? AND slotbin=? AND slotwhere=? AND slotdate=?""")
    turni.forEach {
        val o = it as JSONObject
        println(o)
        val id: String = o.getString("id")
        val slotdate: String = o.getString("slotdate")
        val slotbin: String = o.getString("slotbin")
        val slotwhere: String = o.getString("slotwhere")
        val checked: Boolean = o.getBoolean("checked")
        prepStmt.setString(1, "" + checked)
        prepStmt.setString(2, id)
        prepStmt.setString(3, slotbin)
        prepStmt.setString(4, slotwhere)
        prepStmt.setString(5, slotdate)
        prepStmt.addBatch()
    }
    prepStmt.executeBatch()
    prepStmt.close()

    val stmt = con.createStatement()
    stmt.execute("delete from userindoodle where checked = 'false'")
    stmt.close()
    con.close()
}

fun writeUser(id: String, firstname: String?, lastname: String?, role: String?) {
    val con = getConn()
    val prepStmt = con.prepareStatement("""INSERT INTO doodleuser VALUES ('$id', '$firstname', '$lastname', '$role')""")
    prepStmt.execute()
    prepStmt.close()
    // prepStmt = con.prepareStatement("INSERT INTO userindoodle VALUES (?, ?, ?, ?, ?)")
    // val rs: ResultSet = con.createStatement().executeQuery("select * from doodle")
    // while (rs.next()) {
    //     val slotdate = rs.getString("slotdate")
    //     val slotbin = rs.getString("slotbin")
    //     val slotwhere = rs.getString("slotwhere")
    //     prepStmt.setString(1, id)
    //     prepStmt.setString(2, slotdate)
    //     prepStmt.setString(3, slotbin)
    //     prepStmt.setString(4, slotwhere)
    //     prepStmt.setString(5, "false")
    //     prepStmt.addBatch()
    // }
    // prepStmt.executeBatch()
    // rs.close()
    // prepStmt.close()
    con.close()
}

fun writeTurni() {
    val doodles = getDoodles()
    val con = getConn()
    val prepStmt = con.prepareStatement("INSERT INTO doodle VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
    val formatter = SimpleDateFormat("yyyy-MM-dd")
    val today = formatter.format(Date())
    doodles.doodles.forEach { doodle ->
        val from = doodle.from
        val to = doodle.to
        val datesInRange: MutableList<Date> = mutableListOf()

        val dateFrom: Calendar = getCalendarWithoutTime(formatter.parse(from))
        val dateTo: Calendar = getCalendarWithoutTime(formatter.parse(to))

        while (dateFrom.before(dateTo) || dateFrom.equals(dateTo)) {
            val result: Date = dateFrom.time
            datesInRange += result
            dateFrom.add(Calendar.DATE, 1)
        }

        val formatterE = SimpleDateFormat("E")
        datesInRange.forEach { date ->
            val dateString = formatter.format(date)
            val dayWeek = formatterE.format(date)
            doodle.week.forEach { doodleday ->
                if (doodleday.weekDay == dayWeek) {
                    prepStmt.setString(1, dateString)
                    prepStmt.setInt(2, date.month + 1)
                    prepStmt.setInt(3, date.year + 1900)
                    prepStmt.setInt(4, date.day + 1)
                    prepStmt.setInt(5, SimpleDateFormat("w").format(date).toInt())
                    prepStmt.setString(6, doodleday.bin)
                    prepStmt.setString(7, doodleday.where)
                    prepStmt.setString(8, dayWeek)
                    prepStmt.setString(9, today)
                    prepStmt.addBatch()
                }
            }
        }
        prepStmt.executeBatch()
    }
    prepStmt.close()
    con.close()
}

fun main() {
    println(getDoodles().toString())
}
