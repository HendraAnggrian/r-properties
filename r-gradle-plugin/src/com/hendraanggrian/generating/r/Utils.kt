package com.hendraanggrian.generating.r

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import java.io.File
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

private fun Char.isSymbol(): Boolean = !isDigit() && !isLetter() && !isWhitespace()

private fun String.normalizeSymbols(): String {
    var s = ""
    forEach { s += if (it.isSymbol()) "_" else it }
    return s
}

internal fun File.isValid(): Boolean = !isHidden && name.isNotEmpty() && name.first().isLetter()

internal fun String.normalize(): String = normalizeSymbols()
    .replace("\\s+".toRegex(), " ")
    .trim()

internal fun privateConstructor(): MethodSpec = MethodSpec.constructorBuilder().addModifiers(PRIVATE).build()

internal fun newTypeBuilder(name: String): TypeSpec.Builder = TypeSpec.classBuilder(name.normalize())
    .addModifiers(PUBLIC, STATIC, FINAL)
    .addMethod(privateConstructor())

internal fun TypeSpec.Builder.addStringField(name: String, value: String) {
    val normalizedName = name.normalize()
    if (normalizedName != "_" && // Java SE 9 no longer supports this field name
        normalizedName !in build().fieldSpecs.map { it.name } // checks for duplicate
    ) {
        addField(
            FieldSpec.builder(String::class.java, normalizedName, PUBLIC, STATIC, FINAL)
                .initializer("\$S", value)
                .build()
        )
    }
}