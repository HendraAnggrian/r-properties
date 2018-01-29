package com.hendraanggrian.rsync

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(JUnitPlatform::class)
object ResourceBundleSpec : Spek({

    given("some resource bundle naemes") {
        val names = listOf("string_en", "string_zh", "string_in").map { File("$it.properties") }
        it("is resources bundle") {
            names.forEach { assertTrue(it.isResourceBundle) }
        }
        it("has the same resource bundle name") {
            names.forEach { assertEquals(it.resourceBundleName, "string") }
        }
    }
})