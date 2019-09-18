package com.hendraanggrian.r.adapters

import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.r.isValid
import com.hendraanggrian.r.options.PropertiesOptions
import java.io.File
import java.util.Properties
import javax.lang.model.element.Modifier

internal class PropertiesAdapter(options: PropertiesOptions) : ConfigurableAdapter<PropertiesOptions>(options) {

    override fun adapt(file: File, builder: TypeSpecBuilder): Boolean {
        if (file.extension == "properties") {
            when {
                options.readResourceBundle && file.isResourceBundle() -> {
                    val className = file.resourceBundleName
                    if (className !in builder.build().typeSpecs.map { it.name }) {
                        builder.types.addClass(className) {
                            addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            methods.addConstructor {
                                addModifiers(Modifier.PRIVATE)
                            }
                            process(file)
                        }
                    }
                }
                else -> {
                    builder.process(file)
                    return true
                }
            }
        }
        return false
    }

    private fun TypeSpecBuilder.process(file: File) = file.forEachProperties { key, _ -> stringField(key, key) }

    private fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
        Properties().run {
            load(stream)
            keys.map { it as? String ?: it.toString() }.forEach { key -> action(key, getProperty(key)) }
        }
    }

    private inline val File.resourceBundleName: String get() = nameWithoutExtension.substringBeforeLast("_")

    private fun File.isResourceBundle(): Boolean = isValid() &&
        extension == "properties" &&
        nameWithoutExtension.let { name -> '_' in name && name.substringAfterLast("_").length == 2 }
}