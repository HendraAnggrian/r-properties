package com.hendraanggrian.generating.r.configuration

class PropertiesConfiguration {

    /**
     * When set to true, will convert properties files that are resource bundle.
     * Resource bundle files usually have suffix like `_en`, `_id`.
     *
     * Default value is true, resembles to what works in JavaFX.
     */
    var readResourceBundle: Boolean = true

    /** Groovy-friendly alias of [readResourceBundle]. */
    fun readResourceBundle(readResourceBundle: Boolean) {
        this.readResourceBundle = readResourceBundle
    }
}