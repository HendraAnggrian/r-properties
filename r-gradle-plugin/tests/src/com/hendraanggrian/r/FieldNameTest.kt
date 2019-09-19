package com.hendraanggrian.r

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FieldNameTest {

    @Test
    fun test() {
        assertEquals("nine", "nine".toFieldName())
        assertEquals("_9nine", "9nine".toFieldName())
        assertEquals("n_ne", "n|ne".toFieldName())
        assertEquals("_nine", ">nine".toFieldName())

        assertEquals("string_in_properties", "string_in.properties".toFieldName())

        assertEquals("_final", "final".toFieldName())
        assertEquals("_int", "int".toFieldName())

        assertNull("*".toFieldName())
        assertNull("**".toFieldName())

        assertEquals("text_field", "text-field".toFieldName())
        assertEquals("_text_field", ".text-field".toFieldName())
    }
}