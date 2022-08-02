package it.w4bo

import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class SampleTest {
    @Test
    fun test01() {
        try {
            writeTurni()
            writeUser("foo.bar@gmail.com", "A", "B", "admin")
            val turni = JSONArray()
            val turno = JSONObject()
            turno.put("id", "foo.bar@gmail.com")
            turno.put("slotdate", "2022-08-01")
            turno.put("slotbin", "AM")
            turno.put("slotwhere", "PED")
            turno.put("checked", "true")
            turni.put(turno)
            updateDoodle(turni)
            getTurni()
            writeUser("barz@gmail.com", "C", "D", "admin")
            removeUser("barz@gmail.com")
            removeUser("foo.bar@gmail.com")
        } catch (e: Exception) {
            e.printStackTrace()
            fail()
        }
    }
}