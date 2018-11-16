package com.hendraanggrian.generating.r.configuration

import com.squareup.javapoet.TypeSpec
import java.io.File

data class CustomConfiguration(

    /** When set, will execute [action]. */
    var action: ((typeBuilder: TypeSpec.Builder, file: File) -> Boolean)? = null
)