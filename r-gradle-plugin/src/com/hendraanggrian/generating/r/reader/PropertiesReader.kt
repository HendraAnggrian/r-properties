package com.hendraanggrian.generating.r.reader

import com.hendraanggrian.generating.r.RTask
import com.hendraanggrian.generating.r.isResourceBundle
import com.hendraanggrian.generating.r.newField
import com.hendraanggrian.generating.r.newTypeBuilder
import com.hendraanggrian.generating.r.resourceBundleName
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.util.Properties

@Suppress("Unused")
internal object PropertiesReader : Reader {

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File): Boolean {
        if (file.extension == "properties") {
            when {
                task.properties.supportResourceBundle && file.isResourceBundle() -> {
                    val className = task.name(file.resourceBundleName)
                    if (className !in typeBuilder.build().typeSpecs.map { it.name }) {
                        val innerTypeBuilder = newTypeBuilder(className)
                        process(task, innerTypeBuilder, file)
                        typeBuilder.addType(innerTypeBuilder.build())
                    }
                }
                else -> {
                    process(task, typeBuilder, file)
                    return true
                }
            }
        }
        return false
    }

    private fun process(task: RTask, typeBuilder: TypeSpec.Builder, file: File) =
        file.forEachProperties { key, _ ->
            typeBuilder.addFieldIfNotExist(newField(task.name(key), key))
        }

    private fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
        Properties().run {
            load(stream)
            keys.map { it as? String ?: it.toString() }.forEach { key -> action(key, getProperty(key)) }
        }
    }
}