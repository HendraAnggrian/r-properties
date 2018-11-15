package com.hendraanggrian.generating.r.reader

import com.helger.css.ECSSVersion.CSS30
import com.helger.css.reader.CSSReader.readFromFile
import com.hendraanggrian.generating.r.RTask
import com.hendraanggrian.generating.r.newField
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

@Suppress("Unused")
internal object CSSReader : Reader {

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File): Boolean {
        if (file.extension == "css") {
            val css = checkNotNull(readFromFile(file, UTF_8, CSS30)) {
                "Error while reading css, please report"
            }
            css.allStyleRules.forEach { rule ->
                rule.allSelectors.forEach { selector ->
                    selector.allMembers.forEach { member ->
                        var styleName = member.asCSSString
                        if (task.css.isJavaFx) {
                            styleName = styleName.toFxCssName()
                        }
                        typeBuilder.addFieldIfNotExist(
                            newField(
                                task.name(styleName),
                                styleName
                            )
                        )
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