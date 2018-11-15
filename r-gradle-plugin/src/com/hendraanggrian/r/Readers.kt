package com.hendraanggrian.r

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.util.Properties

fun process(task: RTask, typeBuilder: TypeSpec.Builder, file: File) = file.forEachProperties { key, _ ->
    typeBuilder.addFieldIfNotExist(newField(task.name(key), key))
}

fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
    Properties().run {
        load(stream)
        keys.map { it as? String ?: it.toString() }.forEach { key -> action(key, getProperty(key)) }
    }
}

fun TypeSpec.Builder.addFieldIfNotExist(fieldSpec: FieldSpec) {
    if (fieldSpec.name !in build().fieldSpecs.map { it.name }) {
        addField(fieldSpec)
    }
}