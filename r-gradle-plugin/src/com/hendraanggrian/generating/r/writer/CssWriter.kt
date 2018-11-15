package com.hendraanggrian.generating.r.writer

import com.hendraanggrian.generating.r.RTask
import com.hendraanggrian.generating.r.newField
import com.squareup.javapoet.TypeSpec
import java.io.File

internal object CssWriter : Writer {

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File) = file.forEachLine { line ->
        if (line.length > 2 && line.trimStart().startsWith('.')) {
            var styleName = line.substringAfter('.').trimStart()
            val indexOfSpace = styleName.indexOf(' ')
            val indexOfBracket = styleName.indexOf('{')
            val endIndex = when {
                indexOfSpace != -1 && indexOfSpace < indexOfBracket -> indexOfSpace
                indexOfBracket != -1 && indexOfBracket < indexOfSpace -> indexOfBracket
                else -> null
            }
            if (endIndex != null) {
                styleName = styleName.substring(0, endIndex).trimEnd()
                if (task.name(styleName) !in typeBuilder.build().fieldSpecs.map { it.name }) {
                    typeBuilder.addField(newField(task.name(styleName), styleName))
                }
            }
        }
    }
}