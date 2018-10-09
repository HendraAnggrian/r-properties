package com.hendraanggrian.generation.r

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Multimaps.asMap
import com.hendraanggrian.generation.r.RPlugin.Companion.CLASS_NAME
import com.squareup.javapoet.FieldSpec.builder
import com.squareup.javapoet.JavaFile.builder
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.MethodSpec.constructorBuilder
import com.squareup.javapoet.MethodSpec.methodBuilder
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec.classBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import java.util.Properties
import javax.lang.model.SourceVersion.isName
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

/** R class generation task. */
open class RTask : DefaultTask() {

    /** Actual property of [isLowercase] since annotating [isLowercase] will result in Gradle warning. */
    private var lowercase: Boolean = false

    /**
     * Package name of which R class will be generated to.
     * Default is project group.
     */
    @Input lateinit var packageName: String

    /**
     * Path of resources that will be read.
     * Default is resources folder in main module.
     */
    @InputDirectory lateinit var resourcesDir: File

    /**
     * Will lowercase name of all generated classes and fields in R class.
     * Default is false.
     */
    var isLowercase: Boolean
        @Input get() = lowercase
        @Input set(value) {
            lowercase = value
        }

    /**
     * Collection of files (or directories) that are ignored from this task.
     * Default is empty.
     */
    @InputFiles var exclusions: MutableCollection<File> = mutableListOf()

    /** Exclude certain files and directories from generated R class. */
    @InputFiles fun exclude(vararg files: File): Boolean =
        exclusions.addAll(files.map { resourcesDir.resolve(it) })

    /** Exclude certain files and directories from generated R class. */
    @InputFiles fun exclude(vararg files: String): Boolean =
        exclusions.addAll(files.map { resourcesDir.resolve(it) })

    /**
     * Path that R class will be generated to.
     */
    @OutputDirectory lateinit var outputDir: File

    /** Generate R class given provided options. */
    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        val root = project.projectDir.resolve(resourcesDir)
        requireNotNull(root) { "Resources folder not found" }
        val resources = LinkedHashMultimap.create<String, Pair<String, String>>()
        val resourceBundles = LinkedHashMultimap.create<String, String>()
        outputDir.deleteRecursively()
        root.listFiles()
            .filter { it !in exclusions }
            .forEach { file ->
                when {
                    file.isFile && file.isValid() && file.isResourceBundle() -> {
                        resourceBundles[file.resourceBundleName].add(file.nameWithoutExtension)
                        file.forEachProperties { key, _ ->
                            resources.add(file.resourceBundleName, key, key)
                        }
                    }
                    file.isDirectory -> file.listFiles()
                        .filter { it !in exclusions && it.isFile && it.isValid() }
                        .let { innerFiles ->
                            when (file.name) {
                                "values" -> innerFiles.filter { it.isProperties() }.forEach { innerFile ->
                                    innerFile.forEachProperties { key, value ->
                                        resources.add(innerFile.nameWithoutExtension, key, value)
                                    }
                                }
                                else -> innerFiles.forEach { innerFile ->
                                    resources.add(
                                        file.name,
                                        innerFile.nameWithoutExtension,
                                        "/${file.name}/${innerFile.name}"
                                    )
                                }
                            }
                        }
                }
            }
        builder(packageName, classBuilder(CLASS_NAME)
            .addModifiers(PUBLIC, FINAL)
            .addMethod(privateConstructor())
            .apply {
                asMap(resources).forEach { innerClass, pairs ->
                    addType(classBuilder(innerClass)
                        .addModifiers(PUBLIC, STATIC, FINAL)
                        .addMethod(privateConstructor())
                        .apply {
                            if (resourceBundles.containsKey(innerClass)) {
                                addMethod(
                                    methodBuilder("names")
                                        .addModifiers(PUBLIC, STATIC, FINAL)
                                        .returns(
                                            ParameterizedTypeName.get(
                                                List::class.java,
                                                String::class.java
                                            )
                                        )
                                        .addStatement("return java.util.Arrays.asList(\$L)", resourceBundles[innerClass]
                                            .joinToString(", ") { "\"$it\"" })
                                        .build()
                                )
                            }
                            pairs.forEach { (field, value) ->
                                addField(
                                    builder(String::class.java, field, PUBLIC, STATIC, FINAL)
                                        .initializer("\$S", value)
                                        .build()
                                )
                            }
                        }
                        .build())
                }
            }
            .build())
            .addFileComment("Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            .build()
            .writeTo(outputDir)
    }

    @Internal internal fun isPackageNameCustom(): Boolean = ::packageName.isInitialized

    private fun Multimap<String, Pair<String, String>>.add(innerClassName: String, fieldName: String, value: String) {
        var actualInnerClassName = innerClassName
        var actualFieldName = fieldName.normalizeSymbols()
        if (lowercase) {
            actualInnerClassName = actualInnerClassName.toLowerCase()
            actualFieldName = actualFieldName.toLowerCase()
        }
        check(isName(actualInnerClassName)) { "$innerClassName is not a qualified class name" }
        check(isName(actualFieldName)) { "$fieldName is not a qualified field name" }
        put(actualInnerClassName, actualFieldName to value)
    }

    internal companion object {

        private fun Char.isSymbol(): Boolean = !isDigit() && !isLetter() && !isWhitespace()

        fun String.normalizeSymbols(): String {
            var s = ""
            forEach { s += if (it.isSymbol()) "_" else it }
            return s
        }

        private fun File.isValid(): Boolean = !isHidden && name.isNotEmpty() && Character.isLetter(name.first())

        private fun File.isProperties(): Boolean = extension == "properties"

        fun File.isResourceBundle(): Boolean = isValid() && isProperties() && nameWithoutExtension.let { name ->
            name.contains("_") && name.substringAfterLast("_").length == 2
        }

        inline val File.resourceBundleName: String get() = nameWithoutExtension.substringBeforeLast("_")

        private fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
            Properties().run {
                load(stream)
                keys.map { it as? String ?: it.toString() }.forEach { key -> action(key, getProperty(key)) }
            }
        }

        private fun privateConstructor(): MethodSpec = constructorBuilder().addModifiers(PRIVATE).build()
    }
}