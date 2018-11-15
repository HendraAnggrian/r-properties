package com.hendraanggrian.generating.r.reader

import com.hendraanggrian.generating.r.addFieldIfNotExist
import com.squareup.javapoet.TypeSpec
import java.io.File

internal class DefaultReader(
    private val resourcesPath: String,
    private val usingPrefix: Boolean = false
) : Reader {

    override fun read(typeBuilder: TypeSpec.Builder, file: File): Boolean {
        typeBuilder.addFieldIfNotExist(
            when {
                usingPrefix -> "${file.extension}_${file.nameWithoutExtension}"
                else -> file.nameWithoutExtension
            },
            file.path.substringAfter(resourcesPath)
        )
        return true
    }
}