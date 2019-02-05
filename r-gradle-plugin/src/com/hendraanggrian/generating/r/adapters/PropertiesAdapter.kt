package com.hendraanggrian.generating.r.adapters

import com.hendraanggrian.generating.r.buildInnerTypeSpec
import com.hendraanggrian.generating.r.configuration.PropertiesConfiguration
import com.hendraanggrian.generating.r.isValid
import com.hendraanggrian.generating.r.stringField
import com.hendraanggrian.javapoet.TypeSpecBuilder
import java.io.File
import java.util.Properties

internal class PropertiesAdapter(private val configuration: PropertiesConfiguration) : Adapter {

    override fun adapt(file: File, builder: TypeSpecBuilder): Boolean {
        if (file.extension == "properties") {
            when {
                configuration.readResourceBundle && file.isResourceBundle() -> {
                    val className = file.resourceBundleName
                    if (className !in builder.build().typeSpecs.map { it.name }) {
                        val innerTypeBuilder = buildInnerTypeSpec(file.name)
                        process(innerTypeBuilder, file)
                        builder.nativeBuilder.addType(innerTypeBuilder.build())
                    }
                }
                else -> {
                    process(builder, file)
                    return true
                }
            }
        }
        return false
    }

    private fun process(builder: TypeSpecBuilder, file: File) =
        file.forEachProperties { key, _ ->
            builder.stringField(key, key)
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