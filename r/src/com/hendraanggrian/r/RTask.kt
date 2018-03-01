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
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern
import java.util.Properties
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

open class RTask : DefaultTask() {

    /**
     * Package name of which `r` class will be generated to.
     * Default is project group.
     */
    @Input var packageName: String = project.group.toString()

    /**
     * Path of resources that will be read.
     * Default is resources folder in main module.
     */
    @Input var resourcesDir: String = "src/main/resources"

    /**
     * Path that `R.class` will be generated to.
     */
    @OutputDirectory var outputDir: File = project.buildDir.resolve("$GENERATED_DIRECTORY/r/src/main")

    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        val multimap: Multimap<String, Pair<String, String>> = create()
        outputDir.deleteRecursively()
        project.projectDir.resolve(resourcesDir).listFiles().forEach { file ->
            when {
                file.isFile && file.isValid && file.isResourceBundle -> file.forEachProperties { key, _ ->
                    multimap.add(file.resourceBundleName, key, key)
                }
                file.isDirectory -> file.listFiles().filter { it.isFile && it.isValid }.let { innerFiles ->
                    when (file.name) {
                        "values" -> innerFiles.filter { it.isProperties }.forEach { innerFile ->
                            innerFile.forEachProperties { key, value ->
                                multimap.add(innerFile.nameWithoutExtension, key, value)
                            }
                        }
                        else -> innerFiles.forEach { innerFile ->
                            multimap.add(file.name, innerFile.nameWithoutExtension, "/${file.name}${File.separator}${innerFile.name}")
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

    private fun Multimap<String, Pair<String, String>>.add(cls: String, field: String, value: String) {
        check(SourceVersion.isName(cls)) { "$cls is not a qualified class name" }
        check(SourceVersion.isName(field)) { "$field is not a qualified field name" }
        put(cls, field to value)
    }

    private companion object {
        fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
            val properties = Properties()
            properties.load(stream)
            properties.keys
                .map { it as? String ?: it.toString() }
                .forEach { key -> action(key, properties.getProperty(key)) }
        }
    }
}