package com.hendraanggrian.generating.r.writer

import com.hendraanggrian.generating.r.RTask
import com.hendraanggrian.generating.r.newField
import com.squareup.javapoet.TypeSpec
import java.io.File

internal open class PathWriter(private val prefix: String?) : Writer {

    companion object : PathWriter(null) {
        val PROPERTIES = PathWriter("properties")
        val CSS = PathWriter("css")
    }

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File) {
        typeBuilder.addField(
            newField(
                task.name(prefix?.let { "${it}_" }.orEmpty() + file.nameWithoutExtension),
                file.path.substringAfter(task.resourcesDir.path)
            )
        )
    }
}