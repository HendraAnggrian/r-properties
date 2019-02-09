package com.hendraanggrian.generating.r

import com.hendraanggrian.javapoet.TypeSpecBuilder
import java.io.File
import javax.lang.model.element.Modifier

internal fun Char.isSymbol(): Boolean = !isDigit() && !isLetter() && !isWhitespace()

private fun String.normalizeSymbols(): String {
    var s = ""
    forEach { s += if (it.isSymbol()) "_" else it }
    return s
}

internal fun File.isValid(): Boolean = !isHidden && name.isNotEmpty() && name.first().isLetter()

internal fun String.normalize(): String = normalizeSymbols()
    .replace("\\s+".toRegex(), " ")
    .trim()

internal fun TypeSpecBuilder.stringField(name: String, value: String) {
    val normalizedName = name.normalize()
    if (normalizedName != "_" && // Java SE 9 no longer supports this field name
        normalizedName !in build().fieldSpecs.map { it.name } // checks for duplicate
    ) {
        field<String>(normalizedName) {
            modifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            initializer("\$S", value)
        }
    }
}