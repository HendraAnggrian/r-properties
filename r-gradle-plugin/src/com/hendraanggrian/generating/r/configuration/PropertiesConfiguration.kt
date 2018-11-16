package com.hendraanggrian.generating.r.configuration

data class PropertiesConfiguration(

    /**
     * When set to true, will read properties files that are resource bundle.
     * Resource bundle files usually have suffix like `_en`, `_id`.
     */
    var readResourceBundle: Boolean = false
) {

    /** Groovy-friendly alias of [readResourceBundle]. */
    fun readResourceBundle(readResourceBundle: Boolean) {
        this.readResourceBundle = readResourceBundle
    }
}