package com.hendraanggrian.generating.r.adapters

import com.hendraanggrian.generating.r.stringField
import com.hendraanggrian.javapoet.TypeSpecBuilder
import java.io.File

internal class DefaultAdapter(
    private val resourcesPath: String,
    private val usingPrefix: Boolean = false
) : Adapter {

    override fun adapt(file: File, builder: TypeSpecBuilder): Boolean {
        builder.stringField(
            when {
                usingPrefix -> "${file.extension}_${file.nameWithoutExtension}"
                else -> file.nameWithoutExtension
            },
            file.path.substringAfter(resourcesPath).replace('\\', '/')
        )
        return true
    }
}