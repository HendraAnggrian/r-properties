package com.hendraanggrian.r

import com.google.common.collect.LinkedHashMultimap.create
import com.google.common.collect.Multimap
import com.hendraanggrian.r.RPlugin.Companion.EXTENSION_NAME
import com.squareup.javapoet.FieldSpec.builder
import com.squareup.javapoet.JavaFile.builder
import com.squareup.javapoet.MethodSpec.constructorBuilder
import com.squareup.javapoet.TypeSpec.classBuilder
import java.io.File
import java.io.File.separator
import java.io.Serializable
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern
import java.util.Properties
import javax.lang.model.SourceVersion.isName
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

class RClassWriter internal constructor(
    private val packageName: String,
    private val className: String,
    resources: Array<File>
) : Serializable {

    private val multimap: Multimap<String, Pair<String, String>> = create()

    init {
        resources.forEach { file ->
            when {
                file.isFile && file.isValid && file.isResourceBundle -> file.forEachProperties { key, _ ->
                    add(file.resourceBundleName, key, key)
                }
                file.isDirectory -> file.listFiles().filter { it.isFile && it.isValid }.let { innerFiles ->
                    when (file.name) {
                        "values" -> innerFiles.filter { it.isProperties }.forEach { innerFile ->
                            innerFile.forEachProperties { key, value ->
                                add(innerFile.nameWithoutExtension, key, value)
                            }
                        }
                        else -> innerFiles.forEach { innerFile ->
                            add(file.name, innerFile.nameWithoutExtension, "/${file.name}$separator${innerFile.name}")
                        }
                    }
                }
            }
        }
    }

    internal fun write(outputDirectory: File) = builder(packageName, classBuilder(className)
        .addModifiers(PUBLIC, FINAL)
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .apply {
            multimap.keySet().forEach { innerClass ->
                addType(classBuilder(innerClass)
                    .addModifiers(PUBLIC, STATIC, FINAL)
                    .addMethod(constructorBuilder()
                        .addModifiers(PRIVATE)
                        .build())
                    .apply {
                        multimap.get(innerClass).forEach { (field, value) ->
                            addField(builder(String::class.java, field, PUBLIC, STATIC, FINAL)
                                .initializer("\$S", value)
                                .build())
                        }
                    }
                    .build())
            }
        }
        .build())
        .addFileComment("$EXTENSION_NAME wrote this class at ${now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
        .build()
        .writeTo(outputDirectory)

    private fun add(cls: String, field: String, value: String) {
        check(isName(cls)) { "$cls is not a qualified class name" }
        check(isName(field)) { "$field is not a qualified field name" }
        multimap.put(cls, field to value)
    }

    companion object {
        private fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
            val properties = Properties().apply { load(stream) }
            properties.keys
                .map { it as? String ?: it.toString() }
                .forEach { key -> action(key, properties.getProperty(key)) }
        }
    }
}