package com.hendraanggrian.generating.r.writer

import com.hendraanggrian.generating.r.RTask
import com.hendraanggrian.generating.r.newTypeBuilder
import com.hendraanggrian.generating.r.resourceBundleName
import com.squareup.javapoet.TypeSpec
import java.io.File

internal object ResourceBundlesWriter : PropertiesWriter() {

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File) {
        val className = task.name(file.resourceBundleName)
        if (className !in typeBuilder.build().typeSpecs.map { it.name }) {
            val innerTypeBuilder = newTypeBuilder(className)
            super.read(task, innerTypeBuilder, file)
            typeBuilder.addType(innerTypeBuilder.build())
        }
    }
}