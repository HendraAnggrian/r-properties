package com.hendraanggrian.r.reader

import com.helger.css.ECSSVersion.CSS30
import com.helger.css.reader.CSSReader
import org.junit.Test
import java.nio.charset.StandardCharsets.UTF_8
import kotlin.test.assertEquals

class CssWriterTest {

    @Test fun test() {
        val css = CSSReader.readFromString("div{color:red;}div{color:blue;}", UTF_8, CSS30)!!
        css.allStyleRules.forEach { rule ->
            println(rule)
            rule.allSelectors.forEach {
                it.allMembers.forEach {
                    assertEquals("div", it.asCSSString)
                }
            }
        }
    }
}