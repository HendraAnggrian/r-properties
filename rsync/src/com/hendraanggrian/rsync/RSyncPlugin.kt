package com.hendraanggrian.rsync

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec.constructorBuilder
import com.squareup.javapoet.TypeSpec.classBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.File.separator
import java.nio.file.Files.*
import java.nio.file.Paths.get
import java.time.LocalDateTime.now
import java.util.*
import javax.lang.model.element.Modifier.*

/** Generate Android-like R class with this plugin. */
class RSyncPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create("rsync", RSyncExtension::class.java)
        project.afterEvaluate {
            val outputDir = project.projectDir.resolve(ext.srcDir)
            val oldPath = get(outputDir.absolutePath, *ext.packageName.split('.').toTypedArray(), "${ext.className}.java")
            project.task("rsync").apply {
                doFirst {
                    require(ext.packageName.isNotBlank()) { "Package name must not be blank!" }
                    require(ext.className.isNotBlank()) { "Class name must not be blank!" }
                    deleteIfExists(oldPath)
                }
                doLast {
                    val multimap = LinkedHashMultimap.create<String, Pair<String, String>>()
                    get(project.projectDir.resolve(ext.resDir).absolutePath)
                            .list()
                            .forEach { path ->
                                val file = path.toFile()
                                when {
                                    isRegularFile(path) && file.isValid && file.isResourceBundle -> file.inputStream().use { stream ->
                                        Properties().apply { load(stream) }.keys.forEach { key ->
                                            val s = key as? String ?: key.toString()
                                            multimap.put(file.resourceBundleName, s to s)
                                        }
                                    }
                                    isDirectory(path) -> path
                                            .list()
                                            .map { it.toFile() }
                                            .filter { it.isValid }
                                            .forEach { innerFile ->
                                                val s = "${file.name}$separator${innerFile.name}"
                                                multimap.put(file.name, innerFile.nameWithoutExtension to if (ext.leadingSlash) "/$s" else s)
                                            }
                                }
                            }
                    generateClass(ext.packageName, ext.className, multimap, outputDir)
                }
            }
        }
    }

    companion object {
        private fun generateClass(
                packageName: String,
                className: String,
                multimap: Multimap<String, Pair<String, String>>,
                outputDir: File
        ) = JavaFile.builder(packageName, classBuilder(className)
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
                .addFileComment("rsync generated this class at ${now()}")
                .build()
                .writeTo(outputDir)
    }
}