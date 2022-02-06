package com.example.kozlovdvtest

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun check_fillNullsToIndex() {
        val list = mutableListOf<String?>()
        list.fillNullsToIndex(2)

        assertEquals(listOf(null, null, null), list)
    }

    @Test
    fun check_fillNullsToIndex_def_val_not_add() {
        val list = mutableListOf<String?>("a", "b", "c")
        list.fillNullsToIndex(2)

        assertEquals(listOf("a", "b", "c"), list)
    }

    @Test
    fun check_fillNullsToIndex_def_val() {
        val list = mutableListOf<String?>("a", "b", "c")
        list.fillNullsToIndex(5)

        assertEquals(listOf("a", "b", "c", null, null, null), list)
    }
}