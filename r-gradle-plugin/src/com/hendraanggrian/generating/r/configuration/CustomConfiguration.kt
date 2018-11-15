package com.hendraanggrian.generating.r.configuration

import com.hendraanggrian.generating.r.RTask
import com.squareup.javapoet.TypeSpec
import java.io.File

data class CustomConfiguration(
    var action: ((task: RTask, typeBuilder: TypeSpec.Builder, file: File) -> String?)? = null
)