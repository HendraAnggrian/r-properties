package com.hendraanggrian.r.reader

import com.hendraanggrian.r.isResourceBundle
import com.hendraanggrian.r.newTypeBuilder
import com.hendraanggrian.r.process
import com.hendraanggrian.r.resourceBundleName

@Suppress("Unused")
internal class PropertiesReader(supportResourceBundle: Boolean = false) : Reader<String>({ task, typeBuilder, file ->
    when {
        file.extension == "properties" -> {
            when {
                supportResourceBundle && file.isResourceBundle() -> {
                    val className = task.name(file.resourceBundleName)
                    if (className !in typeBuilder.build().typeSpecs.map { it.name }) {
                        val innerTypeBuilder = newTypeBuilder(className)
                        process(task, innerTypeBuilder, file)
                        typeBuilder.addType(innerTypeBuilder.build())
                    }
                    null
                }
                else -> {
                    process(task, typeBuilder, file)
                    "properties"
                }
            }
        }
        else -> null
    }
})