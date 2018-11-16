package com.hendraanggrian.generating.r.configuration

import com.hendraanggrian.generating.r.addStringField
import com.squareup.javapoet.TypeSpec
import java.io.File

data class CustomConfiguration(

    /** When set, will execute [action]. */
    var action: ((typeBuilder: TypeSpec.Builder, file: File) -> Boolean)? = null
) {

    /** Only to be used within `custom` DSL. */
    fun TypeSpec.Builder.addField(name: String, value: String) = addStringField(name, value)
}