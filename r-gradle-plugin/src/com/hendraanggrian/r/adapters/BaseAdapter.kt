package com.hendraanggrian.r.adapters

import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.r.isFieldName
import com.hendraanggrian.r.toFieldName
import java.io.File
import javax.lang.model.element.Modifier

/** Where the R fields writing process starts, implementation of each adapter may differ. */
internal abstract class BaseAdapter(private val isUppercase: Boolean) {

    abstract fun TypeSpecBuilder.adapt(file: File): Boolean

    protected fun TypeSpecBuilder.addStringField(name: String, value: String) {
        var fieldName: String? = name
        if (isUppercase) {
            fieldName = fieldName!!.toUpperCase()
        }
        if (!fieldName!!.isFieldName()) {
            fieldName = fieldName.toFieldName()
        }
        // checks if field name is valid and there's no duplicate
        if (fieldName != null && fieldName !in build().fieldSpecs.map { it.name }) {
            fields.add<String>(fieldName) {
                addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                initializer("%S", value)
            }
        }
    }
}
