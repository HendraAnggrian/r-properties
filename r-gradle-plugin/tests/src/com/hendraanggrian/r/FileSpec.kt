package com.hendraanggrian.r

import com.hendraanggrian.r.internal.isResourceBundle
import com.hendraanggrian.r.internal.resourceBundleName
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(JUnitPlatform::class)
object FileSpec : Spek({

    given("some resource bundle names") {
        val files = listOf("string_en", "string_zh", "string_in").map { File("$it.properties") }
        it("is resources bundle") {
            files.forEach { assertTrue(it.isResourceBundle()) }
        }
        it("has the same resource bundle name") {
            files.forEach { assertEquals(it.resourceBundleName, "string") }
        }
    }
})