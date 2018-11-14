package com.hendraanggrian.generating.r.readers

import com.hendraanggrian.generating.r.fieldBuilder
import com.squareup.javapoet.TypeSpec
import java.io.File

internal open class Reader {

    companion object : Reader()

    open fun read(typeBuilder: TypeSpec.Builder, file: File, convert: String.() -> String) {
        typeBuilder.addField(
            fieldBuilder(file.nameWithoutExtension.convert())
                .initializer("\$S", file.path)
                .build()
        )
    }
}