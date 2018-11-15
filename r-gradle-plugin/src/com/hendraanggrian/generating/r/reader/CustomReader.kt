package com.hendraanggrian.generating.r.reader

import com.squareup.javapoet.TypeSpec
import java.io.File

internal class CustomReader(
    val action: (typeBuilder: TypeSpec.Builder, file: File) -> Boolean
) : Reader {

    override fun read(typeBuilder: TypeSpec.Builder, file: File): Boolean = action(typeBuilder, file)
}