package com.hendraanggrian.r

import com.google.common.collect.LinkedHashMultimap.create
import com.google.common.collect.Multimap
import com.google.common.collect.Multimaps.asMap
import com.hendraanggrian.r.RPlugin.Companion.CLASS_NAME
import com.hendraanggrian.r.RPlugin.Companion.GENERATED_DIRECTORY
import com.squareup.javapoet.FieldSpec.builder
import com.squareup.javapoet.JavaFile.builder
import com.squareup.javapoet.MethodSpec.constructorBuilder
import com.squareup.javapoet.TypeSpec.classBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.File.separator
import java.io.IOException
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern
import java.util.Properties
import javax.lang.model.SourceVersion.isName
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

/** R class generation task. */
open class RTask : DefaultTask() {

    /**
     * Package name of which R class will be generated to.
     * Default is project group.
     */
    @Input var packageName: String? = null

    /**
     * Path of resources that will be read.
     * Default is resources folder in main module.
     */
    @InputDirectory var resourcesDir: File = project.projectDir.resolve("src/main/resources")

    /**
     * Will lowercase name of all generated classes and fields in R class.
     * Default is false.
     */
    @Input var lowercase: Boolean = false

    /**
     * Collection of files (or directories) that are ignored from this task.
     * Default is empty.
     */
    @InputFiles var exclusions: MutableList<File> = mutableListOf()

    /**
     * Path that R class will be generated to.
     */
    @OutputDirectory var outputDir: File = project.buildDir.resolve("$GENERATED_DIRECTORY/r/src/main")

    /** Exclude certain files and directories from generated R class. */
    fun exclude(vararg files: File): Boolean = exclusions.addAll(files.map { resourcesDir.resolve(it) })

    /** Exclude certain files and directories from generated R class. */
    fun exclude(vararg files: String): Boolean = exclusions.addAll(files.map { resourcesDir.resolve(it) })

    /** Generate R class given provided options. */
    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        val root = project.projectDir.resolve(resourcesDir)
        requireNotNull(root) { "Resources folder not found" }

        val multimap: Multimap<String, Pair<String, String>> = create()
        outputDir.deleteRecursively()
        root.listFiles()
            .filter { it !in exclusions }
            .forEach { file ->
                when {
                    file.isFile && file.isValid() && file.isResourceBundle() -> file.forEachProperties { key, _ ->
                        multimap.add(file.resourceBundleName, key, key)
                    }
                    file.isDirectory -> file.listFiles()
                        .filter { it !in exclusions && it.isFile && it.isValid() }
                        .let { innerFiles ->
                            when (file.name) {
                                "values" -> innerFiles.filter { it.isProperties() }.forEach { innerFile ->
                                    innerFile.forEachProperties { key, value ->
                                        multimap.add(innerFile.nameWithoutExtension, key, value)
                                    }
                                }
                                else -> innerFiles.forEach { innerFile ->
                                    multimap.add(file.name, innerFile.nameWithoutExtension,
                                        "$separator${file.name}$separator${innerFile.name}")
                                }
                            }
                        }
                }
            }
        builder(packageName, classBuilder(CLASS_NAME)
            .addModifiers(PUBLIC, FINAL)
            .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
            .apply {
                asMap(multimap).forEach { innerClass, pairs ->
                    addType(classBuilder(innerClass)
                        .addModifiers(PUBLIC, STATIC, FINAL)
                        .addMethod(constructorBuilder()
                            .addModifiers(PRIVATE)
                            .build())
                        .apply {
                            pairs.forEach { (field, value) ->
                                addField(builder(String::class.java, field, PUBLIC, STATIC, FINAL)
                                    .initializer("\$S", value)
                                    .build())
                            }
                        }
                        .build())
                }
            }
            .build())
            .addFileComment("Generated at ${now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            .build()
            .writeTo(outputDir)
    }

    private fun Multimap<String, Pair<String, String>>.add(innerClassName: String, fieldName: String, value: String) {
        var actualInnerClassName = innerClassName
        var actualFieldName = fieldName.normalizedSymbols
        if (lowercase) {
            actualInnerClassName = actualInnerClassName.toLowerCase()
            actualFieldName = actualFieldName.toLowerCase()
        }
        check(isName(actualInnerClassName)) { "$innerClassName is not a qualified class name" }
        check(isName(actualFieldName)) { "$fieldName is not a qualified field name" }
        put(actualInnerClassName, actualFieldName to value)
    }

    private companion object {

        fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
            Properties().run {
                load(stream)
                keys.map { it as? String ?: it.toString() }.forEach { key -> action(key, getProperty(key)) }
            }
        }
    }
}