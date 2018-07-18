package com.hendraanggrian.generation.r

import com.hendraanggrian.generation.r.internal.normalizeSymbols
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import javax.lang.model.SourceVersion.isName
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(JUnitPlatform::class)
object NameSpec : Spek({

    given("some invalid field names") {
        val names = arrayOf("ABC*DEF", "OpenSans-Regular")
        it("should identify as wrong source name") {
            names.forEach { assertFalse(isName(it)) }
        }
        it("should normalize all symbols") {
            names.forEach { assertTrue(isName(it.normalizeSymbols())) }
        }
    }
})