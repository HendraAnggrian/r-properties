package com.hendraanggrian.r.adapters

import com.hendraanggrian.javapoet.TypeSpecBuilder
import java.io.File

/**
 * An adapter that writes file paths as field values.
 * When optional features are activated (CSS, properties, etc.), underscore prefix will be applied to field names.
 */
internal class PathAdapter(
    isUppercaseField: Boolean,
    private val resourcesDir: String
) : BaseAdapter(isUppercaseField) {

    var isUnderscorePrefix: Boolean = false

    override fun process(typeBuilder: TypeSpecBuilder, file: File): Boolean {
        typeBuilder.addField(
            buildString {
                if (isUnderscorePrefix) append('_')
                append(file.nameWithoutExtension)
            },
            file.path.substringAfter(resourcesDir).replace('\\', '/')
        )
        return true
    }
}
