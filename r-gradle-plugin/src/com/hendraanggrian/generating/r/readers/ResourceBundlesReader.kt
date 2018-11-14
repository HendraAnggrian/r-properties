package com.hendraanggrian.generating.r.readers

import com.hendraanggrian.generating.r.fieldBuilder
import com.hendraanggrian.generating.r.forEachProperties
import com.hendraanggrian.generating.r.newInnerTypeBuilder
import com.squareup.javapoet.TypeSpec
import java.io.File

internal object ResourceBundlesReader : Reader() {

    override fun read(typeBuilder: TypeSpec.Builder, file: File, convert: String.() -> String) {
        val innerTypeBuilder = newInnerTypeBuilder(file.name.convert())
        file.forEachProperties { key, value ->
            innerTypeBuilder.addField(
                fieldBuilder(key.convert())
                    .initializer("\$S", value)
                    .build()
            )
        }
        typeBuilder.addType(innerTypeBuilder.build())
    }
}