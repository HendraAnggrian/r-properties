package com.hendraanggrian.generating.r.configuration

import com.squareup.javapoet.TypeSpec
import java.io.File

data class CustomConfiguration(
    var action: ((typeBuilder: TypeSpec.Builder, file: File) -> Boolean)? = null
)