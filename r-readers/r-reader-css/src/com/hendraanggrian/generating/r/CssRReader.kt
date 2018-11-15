package com.hendraanggrian.generating.r

import com.helger.css.ECSSVersion.CSS30
import com.helger.css.reader.CSSReader
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

class CssRReader : RReader<String> {

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File): String? {
        if (file.extension == "css") {
            val css = checkNotNull(CSSReader.readFromFile(file, UTF_8, CSS30)) {
                "Error while reading css, please report"
            }
            css.allStyleRules.forEach { rule ->
                rule.allSelectors.forEach { selector ->
                    selector.allMembers.forEach { member ->
                        val styleName = member.asCSSString
                        typeBuilder.addFieldIfNotExist(newField(task.name(styleName), styleName))
                    }
                }
            }
            return "css"
        }
        return null
    }
}