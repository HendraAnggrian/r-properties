package com.hendraanggrian.generating.r.configuration

import com.hendraanggrian.generating.r.addStringField
import com.squareup.javapoet.TypeSpec
import java.io.File

class CustomConfiguration {

    /** When set, will execute [action]. */
    var action: ((file: File, typeBuilder: TypeSpec.Builder) -> Boolean)? = null

    /** Only to be used within `custom` DSL. */
    fun TypeSpec.Builder.addField(name: String, value: String) = addStringField(name, value)
}