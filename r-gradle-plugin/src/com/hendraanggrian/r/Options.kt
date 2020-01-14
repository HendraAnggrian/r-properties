package com.hendraanggrian.r

data class CssOptions(
    /**
     * Determine whether adapter should write element type selector (e.g.: `ul { ... }`)
     * It is disabled by default.
     */
    var readElementTypeSelector: Boolean = false,

    /**
     * Determine whether adapter should write class selector (e.g.: `.box { ... }`)
     * It is enabled by default.
     */
    var readClassSelector: Boolean = true,

    /**
     * Determine whether adapter should write ID selector (e.g.: `#container { ... }`)
     * It is enabled by default.
     */
    var readIdSelector: Boolean = true
) {

    /** Groovy-friendly alias of [readElementTypeSelector]. */
    fun readElementTypeSelector(readElementTypeSelector: Boolean) {
        this.readElementTypeSelector = readElementTypeSelector
    }

    /** Groovy-friendly alias of [readClassSelector]. */
    fun readClassSelector(readClassSelector: Boolean) {
        this.readClassSelector = readClassSelector
    }

    /** Groovy-friendly alias of [readIdSelector]. */
    fun readIdSelector(readIdSelector: Boolean) {
        this.readIdSelector = readIdSelector
    }
}

data class JsonOptions(
    /**
     * Determine whether adapter should also write inner json object.
     * It is disabled by default.
     */
    var isRecursive: Boolean = false,

    /**
     * Determine whether adapter should also write inner json array.
     * It is enabled by default. However, has no effect if [isRecursive] is disabled.
     */
    var readArray: Boolean = true
) {

    /** Groovy-friendly alias of [isRecursive]. */
    fun recursive(isRecursive: Boolean) {
        this.isRecursive = isRecursive
    }

    /** Groovy-friendly alias of [readArray]. */
    fun readArray(readArray: Boolean) {
        this.readArray = readArray
    }
}

data class PropertiesOptions(
    /**
     * Determine whether adapter should also write resource bundle.
     * Resource bundle files usually have suffix like `_en`, `_id`.
     * It is disabled by default.
     */
    var readResourceBundle: Boolean = false
) {

    /** Groovy-friendly alias of [readResourceBundle]. */
    fun readResourceBundle(readResourceBundle: Boolean) {
        this.readResourceBundle = readResourceBundle
    }
}
