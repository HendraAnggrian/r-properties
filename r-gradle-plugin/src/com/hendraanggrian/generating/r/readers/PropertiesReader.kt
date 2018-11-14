package com.hendraanggrian.generating.r.readers

import com.hendraanggrian.generating.r.fieldBuilder
import com.hendraanggrian.generating.r.forEachProperties
import com.squareup.javapoet.TypeSpec
import java.io.File

internal object PropertiesReader : Reader() {

    override fun read(typeBuilder: TypeSpec.Builder, file: File, convert: String.() -> String) {
        file.forEachProperties { key, value ->
            typeBuilder.addField(
                fieldBuilder(key.convert())
                    .initializer("\$S", value)
                    .build()
            )
        }
    }
}