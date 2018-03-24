@file:Suppress("NOTHING_TO_INLINE")

package com.hendraanggrian.r

import java.lang.Character.isDigit
import java.lang.Character.isLetter
import java.lang.Character.isSpaceChar

internal val String.normalizedSymbols: String
    get() {
        var s = ""
        forEach { s += if (it.isSymbol()) "_" else it }
        return s
    }

private inline fun Char.isSymbol(): Boolean = !isDigit(this) && !isLetter(this) && !isSpaceChar(this)