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
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.*
import javax.lang.model.element.Modifier.*

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class RSyncPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("rsync", RSyncExtension::class.java)
        project.afterEvaluate {
            // requirement checks
            require(extension.packageName.isNotBlank(), { "Package name must not be blank!" })
            require(extension.className.isNotBlank(), { "Class name must not be blank!" })

            // read resources
            val fileNames = mutableSetOf<String>()
            val fileValuesMap = LinkedHashMultimap.create<String, String>()
            Files.walk(Paths.get(project.projectDir.resolve(extension.pathToResources).absolutePath))
                    .filter { Files.isRegularFile(it) }
                    .map { it.toFile() }
                    .filter { !extension.ignore.contains(it.name) }
                    .forEach {
                        fileNames.add(it.name)
                        when {
                            it.isProperties -> {
                                val stream = it.inputStream()
                                val properties = Properties().apply { load(stream) }
                                stream.close()
                                fileValuesMap.putAll(it.nameWithoutExtension, properties.keys.map { it as? String ?: it.toString() })
                            }
                            else -> fileValuesMap.put(it.extension, it.name)
                        }
                    }

            // handle internationalization
            val resourceBundles = fileValuesMap.keySet().filterInternationalizedProperties()
            val internationalizedMap = LinkedHashMultimap.create<String, String>()
            resourceBundles.distinctInternationalizedPropertiesIdentifier().forEach { key ->
                internationalizedMap.putAll(key, resourceBundles.filter { it.startsWith(key) })
            }
            internationalizedMap.keySet().forEach { key ->
                val temp = mutableListOf<String>()
                internationalizedMap.get(key).forEach { value ->
                    val toBeRemoveds = fileValuesMap.keySet().filter { it == value }
                    toBeRemoveds.forEach { file ->
                        temp.addAll(fileValuesMap.get(file))
                        fileValuesMap.removeAll(file)
                    }
                }
                fileValuesMap.putAll(key, temp)
            }

            // class generation
            val outputDir = project.projectDir.resolve(extension.pathToJava)
            project.tasks.create("rsync").apply {
                outputs.dir(outputDir)
                doLast {
                    Files.deleteIfExists(Paths.get(outputDir.absolutePath, *extension.packageName.split('.').toTypedArray(), extension.className))
                    generateClass(fileNames, fileValuesMap, outputDir, extension.packageName, extension.className)
                }
            }
        }
    }

    companion object {
        private fun generateClass(fileNames: Set<String>, map: Multimap<String, String>, outputDir: File, packageName: String, className: String) {
            val commentBuilder = StringBuilder("rsync generated this class at ${LocalDateTime.now()} from:").appendln()
            fileNames.forEach { commentBuilder.appendln(it) }

            val classBuilder = TypeSpec.classBuilder(className)
                    .addModifiers(PUBLIC, FINAL)
                    .addMethod(MethodSpec.constructorBuilder()
                            .addModifiers(PRIVATE)
                            .build())
            map.keySet().forEach { innerClassName ->
                val innerClassBuilder = TypeSpec.classBuilder(innerClassName)
                        .addModifiers(PUBLIC, STATIC, FINAL)
                        .addMethod(MethodSpec.constructorBuilder()
                                .addModifiers(PRIVATE)
                                .build())
                map.get(innerClassName).forEach { value ->
                    innerClassBuilder.addField(FieldSpec.builder(String::class.java, value.substringBefore('.'), PUBLIC, STATIC, FINAL)
                            .initializer("\$S", value)
                            .build())
                }
                classBuilder.addType(innerClassBuilder.build())
            }

            JavaFile.builder(packageName, classBuilder.build())
                    .addFileComment(commentBuilder.toString())
                    .build()
                    .writeTo(outputDir)
        }
    }
}