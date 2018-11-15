package com.hendraanggrian.generating.r

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.io.Serializable
import java.util.Properties

interface RReader<T> : Serializable {

    fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File): T?

    fun TypeSpec.Builder.addFieldIfNotExist(fieldSpec: FieldSpec) {
        if (fieldSpec.name !in build().fieldSpecs.map { it.name }) {
            addField(fieldSpec)
        }
    }
}

open class DefaultRReader(private val prefix: String?) : RReader<Unit> {

    companion object : DefaultRReader(null)

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File) {
        typeBuilder.addFieldIfNotExist(
            newField(
                task.name(prefix?.let { "${it}_" }.orEmpty() + file.nameWithoutExtension),
                file.path.substringAfter(task.resourcesDir.path)
            )
        )
    }
}

class PropertiesRReader(private val supportResourceBundle: Boolean = true) : RReader<String> {

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File): String? {
        if (file.extension == "properties") {
            when {
                supportResourceBundle && file.isResourceBundle() -> {
                    val className = task.name(file.resourceBundleName)
                    if (className !in typeBuilder.build().typeSpecs.map { it.name }) {
                        val innerTypeBuilder = newTypeBuilder(className)
                        process(task, innerTypeBuilder, file)
                        typeBuilder.addType(innerTypeBuilder.build())
                    }
                }
                else -> {
                    process(task, typeBuilder, file)
                    return "properties"
                }
            }
        }
        return null
    }

    private fun process(task: RTask, typeBuilder: TypeSpec.Builder, file: File) = file.forEachProperties { key, _ ->
        typeBuilder.addFieldIfNotExist(newField(task.name(key), key))
    }

    private fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
        Properties().run {
            load(stream)
            keys.map { it as? String ?: it.toString() }.forEach { key -> action(key, getProperty(key)) }
        }
    }
}