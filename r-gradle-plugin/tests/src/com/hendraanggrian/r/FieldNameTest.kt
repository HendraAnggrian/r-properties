package com.hendraanggrian.r

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FieldNameTest {

    @Test fun test() {
        assertEquals("nine", "nine".toFieldNameOrNull())
        assertEquals("_9nine", "9nine".toFieldNameOrNull())
        assertEquals("n_ne", "n|ne".toFieldNameOrNull())
        assertEquals("_nine", ">nine".toFieldNameOrNull())

        assertEquals("string_in_properties", "string_in.properties".toFieldNameOrNull())

        assertEquals("_final", "final".toFieldNameOrNull())
        assertEquals("_int", "int".toFieldNameOrNull())

        assertNull("*".toFieldNameOrNull())
        assertNull("**".toFieldNameOrNull())

        assertEquals("text_field", "text-field".toFieldNameOrNull())
        assertEquals("_text_field", ".text-field".toFieldNameOrNull())
    }
}