package com.hendraanggrian.generating.r.writer

import com.hendraanggrian.generating.r.RTask
import com.squareup.javapoet.TypeSpec
import java.io.File

internal interface Writer {

    /** Write types or fields inside [typeBuilder] with appropriate [file]. */
    fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File)
}