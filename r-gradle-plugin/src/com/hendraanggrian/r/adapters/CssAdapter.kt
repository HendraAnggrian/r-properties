package com.hendraanggrian.r.adapters

import com.helger.css.ECSSVersion
import com.helger.css.reader.CSSReader
import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.r.CssOptions
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * An adapter that writes CSS classes and identifiers.
 * The file path itself will be written with underscore prefix.
 */
internal class CssAdapter(isUppercase: Boolean, private val options: CssOptions) : BaseAdapter(isUppercase) {

    override fun TypeSpecBuilder.adapt(file: File): Boolean {
        if (file.extension == "css") {
            val css = checkNotNull(CSSReader.readFromFile(file, StandardCharsets.UTF_8, ECSSVersion.CSS30)) {
                "Error while reading css, please report to github.com/hendraanggrian/r-gradle-plugin/issues"
            }
            css.allStyleRules.forEach { rule ->
                rule.allSelectors.forEach { selector ->
                    val member = selector.getMemberAtIndex(0) ?: return false
                    var styleName = member.asCSSString
                    // if (options.isJavaFx) {
                    styleName = styleName.toFxCssName()
                    // }
                    addStringField(styleName, styleName)
                }
            }
            return true
        }
        return false
    }

    private fun String.toFxCssName(): String = when {
        startsWith('.') -> substringAfter('.')
        else -> this
    }
}
