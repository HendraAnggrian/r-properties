package com.hendraanggrian.generating.r.adapters

import com.squareup.javapoet.TypeSpec
import java.io.File

internal interface Adapter {

    fun adapt(file: File, typeBuilder: TypeSpec.Builder): Boolean
}