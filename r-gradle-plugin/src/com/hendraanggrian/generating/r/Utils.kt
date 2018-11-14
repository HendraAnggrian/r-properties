package com.hendraanggrian.generating.r

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.util.Properties
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

fun newInnerTypeBuilder(name: String): TypeSpec.Builder = TypeSpec.classBuilder(name)
    .addModifiers(PUBLIC, STATIC, FINAL)
    .addMethod(privateConstructor())

fun privateConstructor(): MethodSpec = MethodSpec.constructorBuilder().addModifiers(PRIVATE).build()

fun fieldBuilder(name: String): FieldSpec.Builder =
    FieldSpec.builder(String::class.java, name, PUBLIC, STATIC, FINAL)

fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
    Properties().run {
        load(stream)
        keys.map { it as? String ?: it.toString() }.forEach { key -> action(key, getProperty(key)) }
    }
}