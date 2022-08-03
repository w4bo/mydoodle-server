package it.w4bo

import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class SampleTest {
    @Test
    fun test01() {
        try {
            val tokens = listOf("ATsMdSxyZP", "ATsMdSxyZPa")
            writeTurni()
            tokens.forEach { token ->
                writeUser("foo.bar@gmail.com", "A", "B", "admin", token)
                val turni = JSONArray()
                val turno = JSONObject()
                turno.put("id", "foo.bar@gmail.com")
                turno.put("slotdate", "2022-08-01")
                turno.put("slotbin", "AM")
                turno.put("slotwhere", "PED")
                turno.put("checked", "true")
                turno.put("token", token)
                turni.put(turno)
                updateDoodle(turni)
                assertTrue(!getTurni(token, date = "to_date('2022-08-01', 'YYYY-MM-DD')").isEmpty)
                writeUser("barz@gmail.com", "C", "D", "admin", token)
            }
            tokens.forEach { token ->
                removeUser("barz@gmail.com", token)
                removeUser("foo.bar@gmail.com", token)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fail()
        }
    }
}