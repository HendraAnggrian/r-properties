package com.hendraanggrian.generating.r.readers

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec
import java.io.File
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

internal open class Reader {

    companion object : Reader()

    open fun read(typeBuilder: TypeSpec.Builder, file: File) {
        typeBuilder.addField(
            fieldBuilder(file.name)
                .initializer("\$S", file.name)
                .build()
        )
    }

    protected fun fieldBuilder(name: String): FieldSpec.Builder =
        FieldSpec.builder(String::class.java, name, PUBLIC, STATIC, FINAL)
}