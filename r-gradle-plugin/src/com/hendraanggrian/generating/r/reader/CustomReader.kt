package com.hendraanggrian.generating.r.reader

import com.hendraanggrian.generating.r.RTask
import com.squareup.javapoet.TypeSpec
import java.io.File

internal class CustomReader(
    val action: (task: RTask, typeBuilder: TypeSpec.Builder, file: File) -> String?
) : Reader<String> {

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File): String? =
        action(task, typeBuilder, file)
}