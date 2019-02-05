package com.hendraanggrian.generating.r.adapters

import com.hendraanggrian.javapoet.TypeSpecBuilder
import java.io.File

internal interface Adapter {

    fun adapt(file: File, builder: TypeSpecBuilder): Boolean
}