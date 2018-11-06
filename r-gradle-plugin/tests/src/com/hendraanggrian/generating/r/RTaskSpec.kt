package com.hendraanggrian.generating.r

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.File
import javax.lang.model.SourceVersion
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(JUnitPlatform::class)
object RTaskSpec : Spek({

    given("some invalid field names") {
        val names = arrayOf("ABC*DEF", "OpenSans-Regular")
        RTask.run {
            it("should identify as wrong source name") {
                names.forEach { assertFalse(SourceVersion.isName(it)) }
            }
            it("should normalize all symbols") {
                names.forEach { assertTrue(SourceVersion.isName(it.normalizeSymbols())) }
            }
        }
    }

    given("some resource bundle names") {
        val files = listOf("string_en", "string_zh", "string_in").map { File("$it.properties") }
        RTask.run {
            it("is resources bundle") {
                files.forEach { assertTrue(it.isResourceBundle()) }
            }
            it("has the same resource bundle name") {
                files.forEach { assertEquals(it.resourceBundleName, "string") }
            }
        }
    }
})