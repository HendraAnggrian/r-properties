package com.hendraanggrian.rproperties

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import java.io.File
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
object RClassGenerator {

    fun brewJava(keys: Set<Any>, outputDir: File) {
        JavaFile.builder("com.hendraanggrian.rproperties", TypeSpec.classBuilder("R")
                .addModifiers(PUBLIC, FINAL)
                .addType(TypeSpec.classBuilder("string")
                        .addModifiers(PUBLIC, FINAL)
                        .build()).build())
                .addFileComment("Damn you all to hell.")
                .build()
                .writeTo(outputDir)
    }
}