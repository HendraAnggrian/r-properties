package com.hendraanggrian.rsync

import com.google.common.collect.LinkedHashMultimap
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.Assert
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object InternationalizedPropertiesSpec : Spek({

    given("internationalized properties") {
        val resources = listOf(
                "some",
                "some_other",
                "strings_en",
                "strings_zh",
                "strings_in",
                "strings_strings",
                "other_en",
                "other_in"
        )
        it("should filter well") {
            val resourceBundles = resources.filterInternationalizedProperties()
            val multimap = LinkedHashMultimap.create<String, String>()
            resourceBundles.distinctInternationalizedPropertiesIdentifier().forEach { key ->
                multimap.putAll(key, resourceBundles.filter { it.startsWith(key) })
            }
            Assert.assertArrayEquals(
                    multimap.keySet().toTypedArray(),
                    arrayOf("strings", "other"))
            Assert.assertArrayEquals(
                    multimap.values().toTypedArray(),
                    arrayOf("strings_en", "strings_zh", "strings_in", "other_en", "other_in"))
        }
    }
})