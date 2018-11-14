package com.hendraanggrian.generating.r.readers

import com.hendraanggrian.generating.r.RTask
import com.hendraanggrian.generating.r.newField
import com.squareup.javapoet.TypeSpec
import java.io.File

internal open class Reader {

    companion object : Reader()

    open fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File) {
        typeBuilder.addField(
            newField(task.name(file.nameWithoutExtension), file.path.substringAfter(task.resourcesDir.path))
        )
    }
}