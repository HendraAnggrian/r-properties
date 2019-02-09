package com.hendraanggrian.generating.r

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NameTest {

    @Test
    fun names() {
        assertTrue('.'.isSymbol())
        assertEquals("string_in_properties", "string_in.properties".normalize())
    }
}