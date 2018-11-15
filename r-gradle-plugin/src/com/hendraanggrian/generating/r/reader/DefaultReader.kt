package com.hendraanggrian.generating.r.reader

import com.hendraanggrian.generating.r.RTask
import com.hendraanggrian.generating.r.newField
import com.squareup.javapoet.TypeSpec
import java.io.File

internal class DefaultReader(private val prefix: String?) : Reader<Unit> {

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File) {
        typeBuilder.addFieldIfNotExist(
            newField(
                task.name(prefix?.let { "${it}_" }.orEmpty() + file.nameWithoutExtension),
                file.path.substringAfter(task.resourcesDir.path)
            )
        )
    }
}