package com.hendraanggrian.generating.r.configuration

class JsonConfiguration {

    /** When set to true, will convert child json and so on. */
    var isRecursive: Boolean = false

    /** Groovy-friendly alias of [isRecursive]. */
    fun recursive(isRecursive: Boolean) {
        this.isRecursive = isRecursive
    }

    /**
     * When set to true, will also convert json array keys,
     * has no effect if not recursive.
     */
    var readArray: Boolean = true

    /** Groovy-friendly alias of [readArray]. */
    fun readArray(readArray: Boolean) {
        this.readArray = readArray
    }
}