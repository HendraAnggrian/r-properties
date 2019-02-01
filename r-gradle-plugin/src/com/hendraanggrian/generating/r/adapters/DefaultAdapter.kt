package com.hendraanggrian.generating.r.adapters

import com.hendraanggrian.generating.r.addStringField
import com.squareup.javapoet.TypeSpec
import java.io.File

internal class DefaultAdapter(
    private val resourcesPath: String,
    private val usingPrefix: Boolean = false
) : Adapter {

    override fun adapt(file: File, typeBuilder: TypeSpec.Builder): Boolean {
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