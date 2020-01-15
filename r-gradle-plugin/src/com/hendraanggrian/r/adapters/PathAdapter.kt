package com.hendraanggrian.r.adapters

import com.hendraanggrian.javapoet.TypeSpecBuilder
import java.io.File

/**
 * An adapter that writes file paths as field values.
 * When optional features are activated (CSS, properties, etc.), underscore prefix will be applied to field names.
 */
internal class PathAdapter(
    isUppercase: Boolean,
    private val resourcesDir: String,
    private val useUnderscorePrefix: Boolean = false
) : BaseAdapter(isUppercase) {

    override fun TypeSpecBuilder.process(file: File): Boolean {
        addStringField(
            buildString {
                if (useUnderscorePrefix) append('_')
                append(file.nameWithoutExtension)
            },
            file.path.substringAfter(resourcesDir).replace('\\', '/')
        )
        return true
    }
}
