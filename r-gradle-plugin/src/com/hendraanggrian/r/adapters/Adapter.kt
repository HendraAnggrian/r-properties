package com.hendraanggrian.r.adapters

import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.r.normalize
import java.io.File

internal abstract class Adapter {

    abstract fun adapt(file: File, builder: TypeSpecBuilder): Boolean

    fun TypeSpecBuilder.stringField(name: String, value: String) {
        val normalizedName = name.normalize()
        if (normalizedName != "_" && // Java SE 9 no longer supports this field name
            normalizedName !in build().fieldSpecs.map { it.name } // checks for duplicate
        ) {
            field<String>(normalizedName) {
                modifiers = public + static + final
                initializer("\$S", value)
            }
        }
    }
}