package com.hendraanggrian.generating.r.reader

import com.squareup.javapoet.TypeSpec
import java.io.File

internal interface Reader {

    fun read(typeBuilder: TypeSpec.Builder, file: File): Boolean
}