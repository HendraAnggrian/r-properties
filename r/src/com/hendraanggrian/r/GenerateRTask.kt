package com.hendraanggrian.r

import com.google.common.collect.LinkedHashMultimap.create
import com.google.common.collect.Multimap
import com.hendraanggrian.r.RPlugin.Companion.CLASS_NAME
import com.hendraanggrian.r.RPlugin.Companion.EXTENSION_NAME
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile.builder
import com.squareup.javapoet.MethodSpec.constructorBuilder
import com.squareup.javapoet.TypeSpec.classBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.nio.file.Files.isDirectory
import java.nio.file.Files.isRegularFile
import java.nio.file.Files.walk
import java.nio.file.Paths.get
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern
import java.util.Properties
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

open class GenerateRTask : DefaultTask() {

    @Input lateinit var packageName: String
    @Input lateinit var resourcesDir: String
    @OutputDirectory lateinit var outputDir: File

    @TaskAction
    @Throws(IOException::class)
    fun generateR() {
        val multimap = create<String, Pair<String, String>>()
        walk(get(project.projectDir.resolve(resourcesDir).absolutePath))
            .skip(1)
            .forEach { path ->
                val file = path.toFile()
                when {
                    isRegularFile(path) && file.isValid && file.isResourceBundle -> file.forEachProperties { key, _ ->
                        checkValidFieldName(file.resourceBundleName)
                        multimap[file.resourceBundleName] = key to key
                    }
                    isDirectory(path) -> {
                        val innerFiles = file.listFiles().filter { it.isFile }
                        when (file.name) {
                            "values" -> innerFiles.filter { it.isProperties && it.isValid }.forEach { innerFile ->
                                innerFile.forEachProperties { key, value ->
                                    checkValidFieldName(innerFile.nameWithoutExtension)
                                    multimap[innerFile.nameWithoutExtension] = key to value
                                }
                            }
                            else -> innerFiles.filter { it.isValid }.forEach { innerFile ->
                                val s = "${file.name}${File.separator}${innerFile.name}"
                                checkValidFieldName(file.name)
                                multimap[file.name] = innerFile.nameWithoutExtension to "/$s"
                            }
                        }
                    }
                }
            }
        outputDir.deleteRecursively()
        builder(packageName, classBuilder(CLASS_NAME)
            .addModifiers(PUBLIC, FINAL)
            .addMethod(constructorBuilder()
                .addModifiers(PRIVATE)
                .build())
            .apply {
                multimap.keySet().forEach { innerClass ->
                    addType(classBuilder(innerClass)
                        .addModifiers(PUBLIC, STATIC, FINAL)
                        .addMethod(constructorBuilder()
                            .addModifiers(PRIVATE)
                            .build())
                        .apply {
                            multimap.get(innerClass).forEach { (field, value) ->
                                addField(FieldSpec.builder(String::class.java, field, PUBLIC, STATIC, FINAL)
                                    .initializer("\$S", value)
                                    .build())
                            }
                        }
                        .build())
                }
            }
            .build())
            .addFileComment("$EXTENSION_NAME generated this class at ${now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            .build()
            .writeTo(outputDir)
    }

    private fun checkValidFieldName(name: String) = check(SourceVersion.isName(name)) { "Field name is not a valid java variable name" }

    private fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
        val properties = Properties()
        properties.load(stream)
        properties.keys
            .map { it as? String ?: it.toString() }
            .forEach { key -> action(key, properties.getProperty(key)) }
    }

    private operator fun <K, V> Multimap<K, V>.set(key: K, value: V) = put(key, value)
}