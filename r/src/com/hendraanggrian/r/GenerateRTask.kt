package com.hendraanggrian.r

import com.google.common.collect.LinkedHashMultimap.create
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
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths.get
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*
import java.util.stream.Stream
import javax.lang.model.SourceVersion.isName
import javax.lang.model.element.Modifier.*

open class GenerateRTask : DefaultTask() {

    @Input lateinit var packageName: String
    @Input lateinit var resourcesDir: String
    @OutputDirectory lateinit var outputDir: File

    @TaskAction
    @Throws(IOException::class)
    fun generateR() {
        val multimap = create<String, Pair<String, String>>()
        get(project.projectDir.resolve(resourcesDir).absolutePath).children
                .forEach { path ->
                    val file = path.toFile()
                    when {
                        isRegularFile(path) && file.isValid && file.isResourceBundle -> file.inputStream().use { stream ->
                            Properties().apply { load(stream) }.keys.forEach { key ->
                                val s = key as? String ?: key.toString()
                                check(isName(file.resourceBundleName)) { "Field name is not a valid java variable name" }
                                multimap.put(file.resourceBundleName, s to s)
                            }
                        }
                        isDirectory(path) -> path.children
                                .map { it.toFile() }
                                .filter { it.isValid }
                                .forEach { innerFile ->
                                    val s = "${file.name}${File.separator}${innerFile.name}"
                                    check(isName(file.resourceBundleName)) { "Field name is not a valid java variable name" }
                                    multimap.put(file.name, innerFile.nameWithoutExtension to "/$s")
                                }
                    }
                }

        deleteIfExists(outputDir.toPath())
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

    private inline val Path.children: Stream<Path> get() = walk(this).skip(1)
}