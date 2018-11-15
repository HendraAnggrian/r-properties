package com.hendraanggrian.generating.r.writer

import com.hendraanggrian.generating.r.RTask
import com.hendraanggrian.generating.r.newField
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.util.Properties

internal open class PropertiesWriter : Writer {

    companion object : PropertiesWriter()

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File) {
        file.forEachProperties { key, _ ->
            typeBuilder.addField(newField(task.name(key), key))
        }
    }

    private fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
        Properties().run {
            load(stream)
            keys.map { it as? String ?: it.toString() }.forEach { key -> action(key, getProperty(key)) }
        }
    }
}