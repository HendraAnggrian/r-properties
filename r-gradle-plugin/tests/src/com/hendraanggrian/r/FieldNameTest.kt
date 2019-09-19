package com.hendraanggrian.r

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FieldNameTest {

    @Test
    fun old() {
        assertTrue('.'.isSymbol())
        assertEquals("string_in_properties", "string_in.properties".normalize())
    }

    @Test
    fun new() {
        assertEquals("nine", "nine".toFieldName())
        assertEquals("_9nine", "9nine".toFieldName())
        assertEquals("n_ne", "n|ne".toFieldName())
        assertEquals("_nine", ">nine".toFieldName())

        // assertEquals("string_in_properties", "string_in.properties".toFieldName())
    }
}