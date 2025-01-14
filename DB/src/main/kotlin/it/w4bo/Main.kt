package it.w4bo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.github.cdimascio.dotenv.dotenv
import org.json.JSONArray
import org.json.JSONObject
import java.sql.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date
import kotlin.math.roundToInt


enum class type {WEEK, MONTH, YEAR}

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
    val token: String = ""
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

fun getReport(token: String): String {
    val cal: Calendar = GregorianCalendar(Locale.ITALY)
    val today = Date()
    cal.time = today
    val year = cal[Calendar.YEAR]

    var query = """select distinct concat_ws(' ', a.firstname, a.lastname) from doodleuser a order by 1""".trimIndent()

    val tz = TimeZone.getTimeZone("UTC")
    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    df.timeZone = tz
    val nowAsISO = df.format(Date())

    println(query)
    var con = getConn()
    // create the java statement
    val st: Statement = con.createStatement()
    // execute the query, and get a java resultset
    val rs: ResultSet = st.executeQuery(query)
    var res = ""
    while (rs.next()) {
        val who = rs.getString(1)
        val km = if (who.contains("Cavini")) 180 else if (who.contains("Flangini") || who.contains("Bacchi") || who.contains("Dionigi")) 88 else 28
        val eurkm = 0.5
        var cur = "\n---$who---\n![](logo.png)\n\n## Incarico al socio volontario\n\n**Nome e Cognome**: $who\n\n**Motivazione**: Turni in Ospedale\n\n**Luogo**: Ospedale Bufalini\n\n**Centro di Spesa**: Nasi Rossi del Dottor Jumba\n\n|Descrizione|Km|Uscite|\n|-|-|-|\n"
        val query = """
            select concat('|', concat_ws('|', /*id,*/ slotdate/*, weekdayname, slotbin*/, '$km', '${(km * eurkm).roundToInt()}'), '|')
            from (
                select string_agg(concat_ws(' ', a.firstname, a.lastname), ', ') as id, b.slotdate, b.slotbin, b.slotwhere, c.weekdayname, count(*) as count
                from doodleuser a, userindoodle b, doodle c
                where c.slotyear=$year and (c.slotyear = $year or c.slotyear = $year + 1) and a.token = '$token' and a.token = b.u_token and b.d_token = c.token and a.id = b.id and b.slotdate = c.slotdate and b.slotbin = c.slotbin and b.slotwhere = c.slotwhere
                group by b.slotdate, b.slotbin, b.slotwhere, c.weekdayname
                having count(*) > 1
            ) a
            where id like '%$who%'
            order by 1 asc;
        """.trimIndent()

        // create the java statement
        val st2: Statement = con.createStatement()
        // execute the query, and get a java resultset
        val rs2: ResultSet = st2.executeQuery(query)
        var count = 0
        while (rs2.next()) {
            cur += rs2.getString(1) + "\n"
            count += 1
        }
        cur += "\n## Totale complessivo\n\n|Turni|Km|Totale (0.50 Euro/km)|\n|-|-|-|\n|$count|${count * km}|${(count * km * eurkm).roundToInt()}|\n\n*Dichiaro che i dati sono veritieri*\n\n**Data**: ${"$year-12-31" /*nowAsISO*/}\n\n**Firma**"
        if (count > 0) res += cur
    }
    res = res.replace("Mon", "Lun")
        .replace("Tue", "Mar")
        .replace("Wed", "Mer")
        .replace("Thu", "Gio")
        .replace("Fri", "Ven")
        .replace("Sat", "Sab")
        .replace("Sun", "Dom")
    print(res)
    return res
}

fun getTurniFatti(token: String, quando: type): String {
    val cal: Calendar = GregorianCalendar(Locale.ITALY)
    val today = Date()
    cal.time = today

    fun getResult(token: String, quando: type, current: Boolean): Pair<String, Int> {
        val year = cal[Calendar.YEAR]
        val week = "to_date(c.slotdate, 'YYYY-MM-DD') between CURRENT_DATE ${if(current) "-7" else "+1"} and CURRENT_DATE ${if(current) "" else "+7"}"
        val month = cal[Calendar.MONTH] + (if (current) 1 else 2)
        val con = getConn()
        val query =
            """
            select concat_ws(' ', '-', slotdate, weekdayname/*, slotwhere*/, slotbin, id, case when count = 1 then '(turno solitario)' else '' end)
            from (
                select string_agg(concat_ws(' ', a.firstname, a.lastname), ', ') as id, b.slotdate, b.slotbin, b.slotwhere, c.weekdayname, count(*) as count
                from doodleuser a, userindoodle b, doodle c
                where ${if (quando == type.MONTH) "c.slotmonth = $month" else { if (quando==type.YEAR) "c.slotyear=$year" else week }} and (c.slotyear = $year or c.slotyear = $year + 1) and a.token = '$token' and a.token = b.u_token and b.d_token = c.token and a.id = b.id and b.slotdate = c.slotdate and b.slotbin = c.slotbin and b.slotwhere = c.slotwhere
                group by b.slotdate, b.slotbin, b.slotwhere, c.weekdayname
                ${if (quando == type.MONTH || quando == type.YEAR || current) "having count(*) > 1" else ""}
            ) a
            order by 1 asc;
        """.trimIndent()
        println(query)
        // create the java statement
        val st: Statement = con.createStatement()
        // execute the query, and get a java resultset
        val rs: ResultSet = st.executeQuery(query)
        var res = ""
        var count = 0
        while (rs.next()) {
            val turno = rs.getString(1)
            res += "${turno}\n"
            count += if (turno.contains("solitario")) 0 else 1
        }
        res = res
            .replace("Mon", "Lun")
            .replace("Tue", "Mar")
            .replace("Wed", "Mer")
            .replace("Thu", "Gio")
            .replace("Fri", "Ven")
            .replace("Sat", "Sab")
            .replace("Sun", "Dom")
        rs.close()
        st.close()
        con.close()
        return Pair(res, count)
    }

    val turni = getResult(token, quando, quando == type.MONTH)
    var result = """Ciao Nasi!
        
        Turni ${if (quando == type.MONTH) "mensili" else if (quando == type.YEAR) "annuali" else "della prossima settimana"}: ${turni.second}
        ${turni.first}
    """
    result += if (quando != type.WEEK) {
        """
        Chi ha fatto almeno un turno deve:
        - Confermare la correttezza rispondendo a questa e-mail
        - Inviare il RIMBORSO KM ed eventuale RIMBORSO SCONTRINO
        
        PS.
        - Mantenete il doodle aggiornato (e.g., quando salta un turno)
        - Modificate il doodle *senza* duplicare le vostre votazioni (e.g.,
        quando cancellate/aggiungete turni)
        """
    } else {
        """
        Turni della settimana corrente:
        ${getResult(token, quando, true).first}
        
        Se i turni non sono corretti, aggiornate il doodle!
        """
    }
    result = result.trimIndent().replace("[^\\S\\r\\n]+".toRegex(), " ").replace("\\n\\s".toRegex(), "\n")
    println(result)
    return result
}

fun getTurni(token: String, date: String = "now()"): JSONArray {
    val json = JSONArray()
    val con = getConn()
    // val query = "select b.*, a.id, a.checked from userindoodle a join doodle b on (a.slotdate = b.slotdate and a.slotbin = b.slotbin and a.slotwhere = b.slotwhere) order by slotdate, slotwhere, slotbin, id"
    val query = """
                select a.*, coalesce(b.checked, 'false') as checked
                from
                    (
                        select d.*, u.id
                        from doodle d, doodleuser u
                        where d.token = '$token' and d.token = u.token and to_date(d.slotdate, 'YYYY-MM-DD') between ($date - interval '1 month') and ($date + interval '1 month')
                    ) a left outer join userindoodle b on (a.token = b.d_token and a.id = b.id and a.slotdate = b.slotdate and a.slotbin = b.slotbin and a.slotwhere = b.slotwhere)
                order by slotdate, slotwhere, slotbin, id
                """.trimIndent().replace("\\s+".toRegex(), " ")
    // create the java statement
    val st: Statement = con.createStatement()
    // execute the query, and get a java resultset
    val rs: ResultSet = st.executeQuery(query)
    // iterate through the java resultset
    val rsmd: ResultSetMetaData = rs.getMetaData()
    while (rs.next()) {
        val numColumns: Int = rsmd.getColumnCount()
        val obj = JSONObject()
        for (i in 1..numColumns) {
            val columnName: String = rsmd.getColumnName(i)
            obj.put(columnName, rs.getObject(columnName))
        }
        json.put(obj)
    }
    rs.close()
    st.close()
    con.close()
    return json
}

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
    Class.forName("org.postgresql.Driver").newInstance()
    return DriverManager.getConnection(
        "jdbc:postgresql://${dotenv["POSTGRES_IP"]}:${dotenv["POSTGRES_PORT"]}/${dotenv["POSTGRES_DB"]}",
        dotenv["POSTGRES_USER"],
        dotenv["POSTGRES_PASSWORD"]
    )
}

fun updateDoodle(turni: JSONArray) {
    val con = getConn()
    val prepStmt = con.prepareStatement("""INSERT INTO userindoodle (checked, id, slotbin, slotwhere, slotdate, u_token, d_token) values (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id, slotdate, slotbin, slotwhere, u_token) DO UPDATE SET checked = EXCLUDED.checked""")
    // val prepStmt = con.prepareStatement("""UPDATE userindoodle SET checked=? WHERE id=? AND slotbin=? AND slotwhere=? AND slotdate=?""")
    turni.forEach {
        val o = it as JSONObject
        val id: String = o.getString("id")
        val slotdate: String = o.getString("slotdate")
        val slotbin: String = o.getString("slotbin")
        val slotwhere: String = o.getString("slotwhere")
        val checked: Boolean = o.getBoolean("checked")
        val token: String = o.getString("token")
        prepStmt.setString(1, "" + checked)
        prepStmt.setString(2, id)
        prepStmt.setString(3, slotbin)
        prepStmt.setString(4, slotwhere)
        prepStmt.setString(5, slotdate)
        prepStmt.setString(6, token)
        prepStmt.setString(7, token)
        prepStmt.addBatch()
    }
    prepStmt.executeBatch()
    prepStmt.close()

    val stmt = con.createStatement()
    stmt.execute("delete from userindoodle where checked = 'false'")
    stmt.close()
    con.close()
}

fun writeUser(id: String, firstname: String?, lastname: String?, role: String?, token: String): String {
    val con = getConn()
    val prepStmt = con.prepareStatement("""INSERT INTO doodleuser VALUES ('$id', '${firstname ?: id}', '${lastname ?: ""}', '$token', '$role')""")
    prepStmt.execute()
    prepStmt.close()

    val query = """select a.id, a.firstname, a.lastname from doodleuser a where id = '$id' and token = '$token'"""
    val st: Statement = con.createStatement()
    val rs: ResultSet = st.executeQuery(query)
    val rsmd: ResultSetMetaData = rs.metaData
    val obj = JSONObject()
    while (rs.next()) {
        val numColumns: Int = rsmd.columnCount
        for (i in 1..numColumns) {
            val columnName: String = rsmd.getColumnName(i)
            obj.put(columnName, rs.getObject(columnName))
        }
        break
    }
    rs.close()
    st.close()
    con.close()
    return obj.toString()
}

fun removeUser(id: String, token: String) {
    val con = getConn()
    val prepStmt = con.prepareStatement("""DELETE FROM doodleuser where id = '$id' and token = '$token'""")
    prepStmt.execute()
    prepStmt.close()
    con.close()
}

fun writeTurni() {
    val doodles = getDoodles()
    val con = getConn()
    val prepStmt = con.prepareStatement("INSERT INTO doodle VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
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
                    prepStmt.setString(10, doodle.token)
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
