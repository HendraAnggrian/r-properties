package com.hendraanggrian.generating.r.reader

import com.hendraanggrian.generating.r.addStringField
import com.hendraanggrian.generating.r.configuration.PropertiesConfiguration
import com.hendraanggrian.generating.r.isValid
import com.hendraanggrian.generating.r.newTypeBuilder
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.util.Properties

internal class PropertiesReader(private val configuration: PropertiesConfiguration) : Reader {

    override fun read(typeBuilder: TypeSpec.Builder, file: File): Boolean {
        if (file.extension == "properties") {
            when {
                configuration.readResourceBundle && file.isResourceBundle() -> {
                    val className = file.resourceBundleName
                    if (className !in typeBuilder.build().typeSpecs.map { it.name }) {
                        val innerTypeBuilder = newTypeBuilder(className)
                        process(innerTypeBuilder, file)
                        typeBuilder.addType(innerTypeBuilder.build())
                    }
                }
                else -> {
                    process(typeBuilder, file)
                    return true
                }
            }
        }
        return false
    }

    private fun process(typeBuilder: TypeSpec.Builder, file: File) =
        file.forEachProperties { key, _ ->
            typeBuilder.addStringField(key, key)
        }

    private fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
        Properties().run {
            load(stream)
            keys.map { it as? String ?: it.toString() }.forEach { key -> action(key, getProperty(key)) }
        }
    }

    private inline val File.resourceBundleName: String get() = nameWithoutExtension.substringBeforeLast("_")

    private fun File.isResourceBundle(): Boolean = isValid() &&
        extension == "properties" &&
        nameWithoutExtension.let { name ->
            name.contains("_") && name.substringAfterLast("_").length == 2
        }
}