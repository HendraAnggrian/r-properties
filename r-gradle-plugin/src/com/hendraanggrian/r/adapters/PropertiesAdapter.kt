package com.hendraanggrian.r.adapters

import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.r.PropertiesSettings
import java.io.File
import java.util.Properties
import javax.lang.model.element.Modifier

/**
 * An adapter that writes [Properties] keys.
 * The file path itself will be written with underscore prefix.
 */
internal class PropertiesAdapter(isUppercase: Boolean, private val settings: PropertiesSettings) :
    BaseAdapter(isUppercase) {

    override fun TypeSpecBuilder.process(file: File): Boolean {
        if (file.extension == "properties") {
            when {
                settings.isWriteResourceBundle && file.isResourceBundle() -> {
                    val className = file.resourceBundleName
                    if (className !in build().typeSpecs.map { it.name }) {
                        types.addClass(className) {
                            addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            methods.addConstructor { addModifiers(Modifier.PRIVATE) }
                            file.forEachKey { addStringField(it) }
                        }
                    }
                }
                else -> {
                    file.forEachKey { addStringField(it) }
                    return true
                }
            }
        }
        return false
    }

    private fun File.forEachKey(action: (String) -> Unit) = inputStream().use { stream ->
        Properties().run {
            load(stream)
            keys.map { it as? String ?: it.toString() }.forEach(action)
        }
    }

    private val File.resourceBundleName: String get() = nameWithoutExtension.substringBeforeLast("_")

    private fun File.isResourceBundle(): Boolean = !isHidden &&
        extension == "properties" &&
        nameWithoutExtension.let { name -> '_' in name && name.substringAfterLast("_").length == 2 }
}
