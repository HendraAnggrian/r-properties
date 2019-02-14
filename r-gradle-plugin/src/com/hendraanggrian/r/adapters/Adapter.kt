package com.hendraanggrian.r.adapters

import com.hendraanggrian.r.normalize
import com.hendraanggrian.javapoet.TypeSpecBuilder
import java.io.File
import javax.lang.model.element.Modifier

internal abstract class Adapter {

    abstract fun adapt(file: File, builder: TypeSpecBuilder): Boolean

    fun TypeSpecBuilder.stringField(name: String, value: String) {
        val normalizedName = name.normalize()
        if (normalizedName != "_" && // Java SE 9 no longer supports this field name
            normalizedName !in build().fieldSpecs.map { it.name } // checks for duplicate
        ) {
            field<String>(normalizedName) {
                modifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                initializer("\$S", value)
            }
        }
    }
}