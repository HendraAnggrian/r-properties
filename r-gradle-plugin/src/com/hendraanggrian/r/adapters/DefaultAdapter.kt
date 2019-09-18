package com.hendraanggrian.r.adapters

import com.hendraanggrian.javapoet.TypeSpecBuilder
import java.io.File

internal class DefaultAdapter(private val resourcesDir: String, private val usePrefix: Boolean = false) : Adapter() {

    override fun adapt(file: File, builder: TypeSpecBuilder): Boolean {
        builder.stringField(
            when {
                usePrefix -> "${file.extension}_${file.nameWithoutExtension}"
                else -> file.nameWithoutExtension
            },
            file.path.substringAfter(resourcesDir).replace('\\', '/')
        )
        return true
    }
}
