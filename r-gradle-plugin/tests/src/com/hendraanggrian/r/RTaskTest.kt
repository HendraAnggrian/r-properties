package com.hendraanggrian.r

import com.hendraanggrian.r.isResourceBundle
import com.hendraanggrian.r.normalizeSymbols
import com.hendraanggrian.r.resourceBundleName
import org.junit.Test
import java.io.File
import javax.lang.model.SourceVersion
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RTaskTest {

    @Test fun test1() {
        val names = arrayOf("ABC*DEF", "OpenSans-Regular")
        names.forEach { assertFalse(SourceVersion.isName(it)) }
        names.forEach { assertTrue(SourceVersion.isName(it.normalizeSymbols())) }
    }

    @Test fun test2() {
        val files = listOf("string_en", "string_zh", "string_in").map { File("$it.properties") }
        files.forEach { assertTrue(it.isResourceBundle()) }
        files.forEach { assertEquals(it.resourceBundleName, "string") }
    }
}