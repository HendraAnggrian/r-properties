package com.hendraanggrian.generating.r.reader

import com.hendraanggrian.generating.r.RTask
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.io.Serializable

internal interface Reader<T> : Serializable {

    companion object {
        val DEFAULT: Reader<Unit> = DefaultReader(null)
        val ALL: Array<Reader<String>> = arrayOf(CSSReader, JSONReader, PropertiesReader)
    }

    fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File): T?

    fun TypeSpec.Builder.addFieldIfNotExist(fieldSpec: FieldSpec) {
        if (fieldSpec.name !in build().fieldSpecs.map { it.name }) {
            addField(fieldSpec)
        }
    }
}