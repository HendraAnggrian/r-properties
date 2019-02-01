package com.hendraanggrian.generating.r.converters

import com.squareup.javapoet.TypeSpec
import java.io.File

internal interface Converter {

    fun convert(typeBuilder: TypeSpec.Builder, file: File): Boolean
}