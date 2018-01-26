package com.hendraanggrian.rsync

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.File.separator
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths.get
import java.time.LocalDateTime.now
import java.util.*
import java.util.stream.Stream
import javax.lang.model.element.Modifier.*

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
                    val files = LinkedHashMultimap.create<String, Pair<String, String>>()
                    get(project.projectDir.resolve(ext.resDir).absolutePath)
                            .list()
                            .forEach { path ->
                                val root = path.toFile()
                                when {
                                    isRegularFile(path) && root.isValid && root.isResourceBundle -> root.inputStream().use { stream ->
                                        Properties().apply { load(stream) }.keys.forEach { key ->
                                            val s = key as? String ?: key.toString()
                                            files.put(root.resourceBundleName, s to s)
                                        }
                                    }
                                    isDirectory(path) -> path
                                            .list()
                                            .map { it.toFile() }
                                            .filter { it.isValid }
                                            .forEach { file ->
                                                val s = "${root.name}$separator${file.name}"
                                                files.put(root.name, file.nameWithoutExtension to if (ext.leadingSlash) "/$s" else s)
                                            }
                                }
                            }
                    generateClass(ext.packageName, ext.className, files, outputDir)
                }
            }
        }
    }

    companion object {
        private fun generateClass(packageName: String, className: String, map: Multimap<String, Pair<String, String>>, outputDir: File) = JavaFile
                .builder(packageName, TypeSpec.classBuilder(className)
                        .addModifiers(PUBLIC, FINAL)
                        .addMethod(MethodSpec.constructorBuilder()
                                .addModifiers(PRIVATE)
                                .build())
                        .apply {
                            map.keySet().forEach { innerClassName ->
                                addType(TypeSpec.classBuilder(innerClassName)
                                        .addModifiers(PUBLIC, STATIC, FINAL)
                                        .addMethod(MethodSpec.constructorBuilder()
                                                .addModifiers(PRIVATE)
                                                .build())
                                        .apply {
                                            map.get(innerClassName).forEach { (field, value) ->
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

        private fun Path.list(): Stream<Path> = walk(this).skip(1)

        private inline val File.isValid: Boolean get() = name != ".DS_Store"

        private inline val File.isResourceBundle: Boolean
            get() = extension == "properties"
                    && nameWithoutExtension.contains("_")
                    && nameWithoutExtension.substring(nameWithoutExtension.lastIndexOf('_') + 1).length == 2

        private inline val File.resourceBundleName: String
            get() = nameWithoutExtension.substring(0, nameWithoutExtension.lastIndexOf('_'))
    }
}