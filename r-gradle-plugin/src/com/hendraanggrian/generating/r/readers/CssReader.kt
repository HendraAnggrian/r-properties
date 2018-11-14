package com.hendraanggrian.generating.r.readers

import com.hendraanggrian.generating.r.RTask
import com.hendraanggrian.generating.r.newField
import com.squareup.javapoet.TypeSpec
import java.io.File

internal object CssReader : Reader() {

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File) = file.forEachLine {
        if (it.length > 2 && it.trimStart().startsWith('.')) {
            val styleName = it.substringAfter('.').substringBefore('{').trim()
            typeBuilder.addField(newField(task.name(styleName), styleName))
        }
    }
}