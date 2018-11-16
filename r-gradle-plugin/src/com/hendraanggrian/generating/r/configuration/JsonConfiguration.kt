package com.hendraanggrian.generating.r.configuration

data class JsonConfiguration(

    /** When set to true, will read child json and so on. */
    var isRecursive: Boolean = false,

    /**
     * When set to true, will also read json array keys,
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