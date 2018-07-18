@file:Suppress("NOTHING_TO_INLINE")

package com.hendraanggrian.generation.r.internal

import java.lang.Character.isDigit
import java.lang.Character.isLetter
import java.lang.Character.isSpaceChar

internal fun String.normalizeSymbols(): String {
    var s = ""
    forEach { s += if (it.isSymbol()) "_" else it }
    return s
}

private inline fun Char.isSymbol(): Boolean = !isDigit(this) && !isLetter(this) && !isSpaceChar(this)