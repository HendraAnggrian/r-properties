package com.hendraanggrian.r.adapters

import com.helger.css.ECSSVersion.CSS30
import com.helger.css.reader.CSSReader.readFromFile
import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.r.options.CssOptions
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

internal class CssAdapter(options: CssOptions) : ConfigurableAdapter<CssOptions>(options) {

    override fun adapt(file: File, builder: TypeSpecBuilder): Boolean {
        if (file.extension == "css") {
            val css = checkNotNull(readFromFile(file, UTF_8, CSS30)) {
                "Error while reading css, please report"
            }
            css.allStyleRules.forEach { rule ->
                rule.allSelectors.forEach { selector ->
                    val member = selector.getMemberAtIndex(0) ?: return false
                    var styleName = member.asCSSString
                    if (options.isJavaFx) {
                        styleName = styleName.toFxCssName()
                    }
                    builder.stringField(styleName, styleName)
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
