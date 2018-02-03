package com.hendraanggrian.r

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import com.squareup.javapoet.FieldSpec.builder
import com.squareup.javapoet.MethodSpec.constructorBuilder
import com.squareup.javapoet.TypeSpec.classBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.File.separator
import java.nio.file.Files.*
import java.nio.file.Paths.get
import java.util.*
import javax.lang.model.SourceVersion.isName
import javax.lang.model.element.Modifier.*

/** Generate Android-like R class with this plugin. */
class RPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create("r", RExtension::class.java)
        project.afterEvaluate {
            project.task("r").apply {
                val outputDir = project.projectDir.resolve(ext.srcFile)
                doFirst {
                    deleteIfExists(get(outputDir.absolutePath, *ext.packagePaths, "R.java"))
                }
                doLast {
                    val multimap = LinkedHashMultimap.create<String, Pair<String, String>>()
                    get(project.projectDir.resolve(ext.resFile).absolutePath)
                            .list()
                            .forEach { path ->
                                val file = path.toFile()
                                when {
                                    isRegularFile(path) && file.isValid && file.isResourceBundle -> file.inputStream().use { stream ->
                                        Properties().apply { load(stream) }.keys.forEach { key ->
                                            val s = key as? String ?: key.toString()
                                            check(isName(file.resourceBundleName)) { "Field name is not a valid java variable name!" }
                                            multimap.put(file.resourceBundleName, s to s)
                                        }
                                    }
                                    isDirectory(path) -> path
                                            .list()
                                            .map { it.toFile() }
                                            .filter { it.isValid }
                                            .forEach { innerFile ->
                                                val s = "${file.name}$separator${innerFile.name}"
                                                check(isName(file.resourceBundleName)) { "Field name is not a valid java variable name!" }
                                                multimap.put(file.name, innerFile.nameWithoutExtension to ext.getPath(s))
                                            }
                                }
                            }
                    ext.generateClass(multimap, outputDir)
                }
            }
        }
    }

    companion object {
        private fun RExtension.generateClass(multimap: Multimap<String, Pair<String, String>>, outputDir: File) = toJavaFile(classBuilder("R")
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
                                        addField(builder(String::class.java, field, PUBLIC, STATIC, FINAL)
                                                .initializer("\$S", value)
                                                .build())
                                    }
                                }
                                .build())
                    }
                }
                .build())
                .writeTo(outputDir)
    }
}