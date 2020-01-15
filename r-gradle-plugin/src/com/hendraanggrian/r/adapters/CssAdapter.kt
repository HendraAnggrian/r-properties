package com.hendraanggrian.r.adapters

import com.helger.css.reader.CSSReader
import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.r.CssSettings
import java.io.File

/**
 * An adapter that writes CSS classes and identifiers.
 * The file path itself will be written with underscore prefix.
 */
internal class CssAdapter(isUppercase: Boolean, private val settings: CssSettings) : BaseAdapter(isUppercase) {

    override fun TypeSpecBuilder.process(file: File): Boolean {
        if (file.extension == "css") {
            val css = checkNotNull(CSSReader.readFromFile(file, settings.charset, settings.cssVersion)) {
                "Error while reading CSS, please report to github.com/hendraanggrian/r-gradle-plugin/issues"
            }
            css.allStyleRules.forEach { rule ->
                rule.allSelectors.forEach { selector ->
                    val member = selector.getMemberAtIndex(0)?.asCSSString ?: return false
                    when {
                        member.startsWith('.') -> if (settings.isWriteClassSelector)
                            addStringField(member.substringAfter('.'))
                        member.startsWith('#') -> if (settings.isWriteIdSelector)
                            addStringField(member.substringAfter('#'))
                        else -> if (settings.isWriteElementTypeSelector) addStringField(member)
                    }
                }
            }
            return true
        }
        return false
    }
}
