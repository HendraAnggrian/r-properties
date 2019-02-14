package com.hendraanggrian.r

import java.io.File

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