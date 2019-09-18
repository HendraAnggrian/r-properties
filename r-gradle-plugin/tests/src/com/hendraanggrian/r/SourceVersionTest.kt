package com.hendraanggrian.r

import javax.lang.model.SourceVersion
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SourceVersionTest {

    @Test
    fun test() {
        assertFalse(SourceVersion.isName("my-resource"))
        assertTrue(SourceVersion.isName("myResource"))
    }
}