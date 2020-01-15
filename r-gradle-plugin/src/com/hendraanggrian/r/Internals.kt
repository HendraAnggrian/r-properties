package com.hendraanggrian.r

import javax.lang.model.SourceVersion

/** Check if string is a valid Java field name. */
internal fun String.isFieldName(): Boolean = when {
    isEmpty() || this == "_" || !SourceVersion.isName(this) -> false // Java SE 9 no longer supports '_'
    else -> first().isJavaIdentifierStart() && drop(1).all { it.isJavaIdentifierPart() }
}

/** Fixes invalid field name, or null if it is un-fixable. */
internal fun String.toFieldNameOrNull(): String? {
    var result = this
    // Return original string if already valid
    if (result.isFieldName()) {
        return result
    }
    // Append underscore if first char is not java identifier start
    if (!result.first().isJavaIdentifierStart()) {
        result = "_$result"
    }
    // Convert all non-java identifier part chars to underscore
    result = result.map { if (it.isJavaIdentifierPart()) it else '_' }.joinToString("")
    // Merge duplicate underscores
    while ("__" in result) {
        result = result.replace("__", "_")
    }
    // Append underscore to keyword and literal
    if (!SourceVersion.isName(result)) {
        result = "_$result"
    }
    // Return successfully fixed string, or null if unfixable
    return when {
        result.isFieldName() -> result
        else -> null
    }
}
