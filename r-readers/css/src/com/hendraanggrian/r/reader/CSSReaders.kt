package com.hendraanggrian.r.reader

import com.helger.css.ECSSVersion.CSS30
import com.helger.css.reader.CSSReader
import com.hendraanggrian.r.addFieldIfNotExist
import com.hendraanggrian.r.newField
import java.nio.charset.StandardCharsets.UTF_8

@Suppress("Unused")
class CSSReader(isJavaFxCss: Boolean = false) : Reader<String>({ task, typeBuilder, file ->
    when {
        file.extension == "css" -> {
            val css = checkNotNull(CSSReader.readFromFile(file, UTF_8, CSS30)) {
                "Error while reading css, please report"
            }
            css.allStyleRules.forEach { rule ->
                rule.allSelectors.forEach { selector ->
                    selector.allMembers.forEach { member ->
                        var styleName = member.asCSSString
                        if (isJavaFxCss) {
                            styleName = styleName.toFxCssName()
                        }
                        typeBuilder.addFieldIfNotExist(newField(task.name(styleName), styleName))
                    }
                }
            }
            "css"
        }
        else -> null
    }
})

private fun String.toFxCssName(): String = when {
    startsWith('.') -> substringAfter('.')
    else -> this
}