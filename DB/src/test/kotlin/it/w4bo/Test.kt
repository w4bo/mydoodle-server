package it.w4bo

import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class SampleTest {
    @Test
    fun test01() {
        try {
            writeTurni()
        } catch (e: Exception) {
            e.printStackTrace()
            fail()
        }
    }
}