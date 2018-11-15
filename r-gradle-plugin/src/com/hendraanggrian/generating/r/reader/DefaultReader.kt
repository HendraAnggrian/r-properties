package com.hendraanggrian.generating.r.reader

import com.hendraanggrian.generating.r.RTask
import com.hendraanggrian.generating.r.newField
import com.squareup.javapoet.TypeSpec
import java.io.File

internal class DefaultReader(private val usingPrefix: Boolean) : Reader {

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File): Boolean {
        typeBuilder.addFieldIfNotExist(
            newField(
                task.name(
                    when {
                        usingPrefix -> "${file.extension}_${file.nameWithoutExtension}"
                        else -> file.nameWithoutExtension
                    }
                ),
                file.path.substringAfter(task.resourcesDir.path)
            )
        )
        return true
    }
}