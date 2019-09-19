package com.hendraanggrian.r

import java.io.File
import javax.lang.model.SourceVersion

internal fun Char.isSymbol(): Boolean =
    !isDigit() && !isLetter() && !isWhitespace()

private fun String.normalizeSymbols(): String {
    var s = ""
    forEach { s += if (it.isSymbol()) "_" else it }
    return s
}

internal fun File.isValid(): Boolean = !isHidden &&
    nameWithoutExtension.isNotEmpty() &&
    nameWithoutExtension.first().isJavaIdentifierStart()

internal fun String.normalize(): String = normalizeSymbols()
    .replace("\\s+".toRegex(), " ")
    .trim()

/** Fixes invalid field name, returns original string if field name is already valid. */
internal fun String.toFieldName(): String {
    var result = this
    if (result.isEmpty() || SourceVersion.isName(result)) {
        return result
    }
    if (!result.first().isJavaIdentifierStart()) {
        result = "_$result"
    }
    println()
    println()
    return result.map {
        println("$it -> ${it.isJavaIdentifierPart()}")
        when {
            it.isJavaIdentifierPart() -> it
            else -> '_'
        }
    }.joinToString("")
}