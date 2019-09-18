package com.hendraanggrian.r

data class CssOptions(
    /** When set to true, will remove `.` prefix in JavaFX CSS style classes. */
    var isJavaFx: Boolean = false
) {

    /** Groovy-friendly alias of [isJavaFx]. */
    fun javaFx(isJavaFx: Boolean) {
        this.isJavaFx = isJavaFx
    }
}

data class JsonOptions(
    /** When set to true, will convert child json and so on. */
    var isRecursive: Boolean = false,
    /**
     * When set to true, will also convert json array keys,
     * has no effect if not recursive.
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
     * When set to true, will convert properties files that are resource bundle.
     * Resource bundle files usually have suffix like `_en`, `_id`.
     *
     * Default value is true, resembles to what works in JavaFX.
     */
    var readResourceBundle: Boolean = true
) {

    /** Groovy-friendly alias of [readResourceBundle]. */
    fun readResourceBundle(readResourceBundle: Boolean) {
        this.readResourceBundle = readResourceBundle
    }
}
