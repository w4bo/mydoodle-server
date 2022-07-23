package it.w4bo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.github.cdimascio.dotenv.dotenv
import java.sql.DriverManager
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

fun writeTurni() {
    val doodles = getDoodles()
    val dotenv = dotenv()

    Class.forName("com.mysql.jdbc.Driver").newInstance()
    val conn = DriverManager.getConnection(
        "jdbc:mysql://${dotenv["MYSQL_IP"]}:${dotenv["MYSQL_PORT"]}/${dotenv["MYSQL_DB"]}",
        dotenv["MYSQL_USER"],
        dotenv["MYSQL_PWD"]
    )
    val prepStmt = conn.prepareStatement("INSERT INTO doodle VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
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
}

fun main() {
    println(getDoodles().toString())
}
