package com.hendraanggrian.generating.r.converters

import com.squareup.javapoet.TypeSpec
import java.io.File

internal class CustomConverter(val action: (typeBuilder: TypeSpec.Builder, file: File) -> Boolean) : Converter {

    override fun convert(typeBuilder: TypeSpec.Builder, file: File): Boolean = action(typeBuilder, file)
}