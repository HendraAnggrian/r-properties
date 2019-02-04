package com.hendraanggrian.generating.r.adapters

import com.squareup.javapoet.TypeSpec
import java.io.File

internal class CustomAdapter(val action: (file: File, typeBuilder: TypeSpec.Builder) -> Boolean) : Adapter {

    override fun adapt(file: File, typeBuilder: TypeSpec.Builder): Boolean = action(file, typeBuilder)
}