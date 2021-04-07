package com.hendraanggrian.r

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FieldNameTest {

    @Test
    fun test() {
        assertEquals("nine", "nine".toJavaNameOrNull())
        assertEquals("_9nine", "9nine".toJavaNameOrNull())
        assertEquals("n_ne", "n|ne".toJavaNameOrNull())
        assertEquals("_nine", ">nine".toJavaNameOrNull())

        assertEquals("string_in_properties", "string_in.properties".toJavaNameOrNull())

        assertEquals("_final", "final".toJavaNameOrNull())
        assertEquals("_int", "int".toJavaNameOrNull())

        assertNull("*".toJavaNameOrNull())
        assertNull("**".toJavaNameOrNull())

        assertEquals("text_field", "text-field".toJavaNameOrNull())
        assertEquals("_text_field", ".text-field".toJavaNameOrNull())
    }
}