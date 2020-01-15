package com.hendraanggrian.r

import com.helger.css.ECSSVersion
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Settings for customizing the field generation of CSS file.
 *
 * @see RTask.configureCss
 */
data class CssSettings(
    /**
     * The charset to be used in case neither a [charset] rule nor a BOM is present.
     * Default value is [StandardCharsets.UTF_8].
     */
    var charset: Charset = StandardCharsets.UTF_8,

    /**
     * The CSS version to use.
     * Default value is [ECSSVersion.CSS30].
     */
    var cssVersion: ECSSVersion = ECSSVersion.CSS30,

    /**
     * Determine whether adapter should write element type selector (e.g.: `ul { ... }`)
     * It is disabled by default.
     */
    var isWriteElementTypeSelector: Boolean = false,

    /**
     * Determine whether adapter should write class selector (e.g.: `.box { ... }`)
     * It is enabled by default.
     */
    var isWriteClassSelector: Boolean = true,

    /**
     * Determine whether adapter should write ID selector (e.g.: `#container { ... }`)
     * It is enabled by default.
     */
    var isWriteIdSelector: Boolean = true
) {

    /** Groovy-friendly alias of [charset]. */
    fun charset(charset: Charset) {
        this.charset = charset
    }

    /** Groovy-friendly alias of [cssVersion]. */
    fun cssVersion(cssVersion: ECSSVersion) {
        this.cssVersion = cssVersion
    }

    /** Groovy-friendly alias of [isWriteElementTypeSelector]. */
    fun writeElementTypeSelector(writeElementTypeSelector: Boolean) {
        isWriteElementTypeSelector = writeElementTypeSelector
    }

    /** Groovy-friendly alias of [isWriteClassSelector]. */
    fun writeClassSelector(writeClassSelector: Boolean) {
        isWriteClassSelector = writeClassSelector
    }

    /** Groovy-friendly alias of [isWriteIdSelector]. */
    fun writeIdSelector(writeIdSelector: Boolean) {
        isWriteIdSelector = writeIdSelector
    }
}

/**
 * Settings for customizing the field generation of json file.
 *
 * @see RTask.configureJson
 */
data class JsonSettings(
    /**
     * Determine whether adapter should also write inner json object.
     * It is disabled by default.
     */
    var isRecursive: Boolean = false,

    /**
     * Determine whether adapter should also write inner json array.
     * It is enabled by default. However, has no effect if [isRecursive] is disabled.
     */
    var isWriteArray: Boolean = true
) {

    /** Groovy-friendly alias of [isRecursive]. */
    fun recursive(recursive: Boolean) {
        isRecursive = recursive
    }

    /** Groovy-friendly alias of [isWriteArray]. */
    fun writeArray(writeArray: Boolean) {
        isWriteArray = writeArray
    }
}

/**
 * Settings for customizing the field generation of properties file.
 *
 * @see RTask.configureProperties
 */
data class PropertiesSettings(
    /**
     * Determine whether adapter should also write resource bundle.
     * Resource bundle files usually have suffix like `_en`, `_id`.
     * It is disabled by default.
     */
    var isWriteResourceBundle: Boolean = false
) {

    /** Groovy-friendly alias of [isWriteResourceBundle]. */
    fun writeResourceBundle(writeResourceBundle: Boolean) {
        isWriteResourceBundle = writeResourceBundle
    }
}
