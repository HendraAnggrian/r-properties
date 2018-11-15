package com.hendraanggrian.generating.r.reader

import com.helger.css.ECSSVersion.CSS30
import com.helger.css.reader.CSSReader.readFromFile
import com.hendraanggrian.generating.r.addFieldIfNotExist
import com.hendraanggrian.generating.r.configuration.CSSConfiguration
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

internal class CSSReader(private val configuration: CSSConfiguration) : Reader {

    override fun read(typeBuilder: TypeSpec.Builder, file: File): Boolean {
        if (file.extension == "css") {
            val css = checkNotNull(readFromFile(file, UTF_8, CSS30)) {
                "Error while reading css, please report"
            }
            css.allStyleRules.forEach { rule ->
                rule.allSelectors.forEach { selector ->
                    selector.allMembers.forEach { member ->
                        var styleName = member.asCSSString
                        if (configuration.isJavaFx) {
                            styleName = styleName.toFxCssName()
                        }
                        typeBuilder.addFieldIfNotExist(styleName, styleName)
                    }
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