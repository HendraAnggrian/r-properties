package com.hendraanggrian.generating.r.readers

import com.squareup.javapoet.TypeSpec
import java.io.File
import java.util.Properties

internal object PropertiesReader : Reader() {

    override fun read(typeBuilder: TypeSpec.Builder, file: File) {
        file.forEachProperties { key, value ->
            typeBuilder.addField(
                fieldBuilder(key)
                    .initializer("\$S", value)
                    .build()
            )
        }
    }

    private fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
        Properties().run {
            load(stream)
            keys.map { it as? String ?: it.toString() }.forEach { key -> action(key, getProperty(key)) }
        }
    }
}