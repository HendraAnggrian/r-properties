package com.hendraanggrian.generating.r.reader

import com.squareup.javapoet.TypeSpec
import java.io.File
import java.io.Serializable

internal interface Reader : Serializable {

    fun read(typeBuilder: TypeSpec.Builder, file: File): Boolean
}