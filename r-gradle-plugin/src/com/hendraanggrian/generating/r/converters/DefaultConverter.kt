package com.hendraanggrian.generating.r.converters

import com.hendraanggrian.generating.r.addStringField
import com.squareup.javapoet.TypeSpec
import java.io.File

internal class DefaultConverter(
    private val resourcesPath: String,
    private val usingPrefix: Boolean = false
) : Converter {

    override fun convert(typeBuilder: TypeSpec.Builder, file: File): Boolean {
        typeBuilder.addStringField(
            when {
                usingPrefix -> "${file.extension}_${file.nameWithoutExtension}"
                else -> file.nameWithoutExtension
            },
            file.path.substringAfter(resourcesPath).replace('\\', '/')
        )
        return true
    }
}