package com.hendraanggrian.generating.r

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import java.io.File
import javax.lang.model.element.Modifier

fun privateConstructor(): MethodSpec = MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build()

fun newTypeBuilder(name: String): TypeSpec.Builder = TypeSpec.classBuilder(name)
    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
    .addMethod(privateConstructor())

fun newField(name: String, value: String): FieldSpec =
    FieldSpec.builder(String::class.java, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .initializer("\$S", value)
        .build()

private fun Char.isSymbol(): Boolean = !isDigit() && !isLetter() && !isWhitespace()

fun String.normalizeSymbols(): String {
    var s = ""
    forEach { s += if (it.isSymbol()) "_" else it }
    return s
}

fun File.isValid(): Boolean = !isHidden && name.isNotEmpty() && name.first().isLetter()

fun File.isResourceBundle(): Boolean = isValid() && extension == "properties" && nameWithoutExtension.let { name ->
    name.contains("_") && name.substringAfterLast("_").length == 2
}

inline val File.resourceBundleName: String get() = nameWithoutExtension.substringBeforeLast("_")