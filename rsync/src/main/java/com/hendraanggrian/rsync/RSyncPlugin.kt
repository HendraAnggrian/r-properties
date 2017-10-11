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
        val ext = project.extensions.create("rsync", RSyncExtension::class.java)
        project.afterEvaluate {
            // requirement checks
            require(ext.packageName.isNotBlank(), { "Package name must not be blank!" })
            require(ext.className.isNotBlank(), { "Class name must not be blank!" })

            // read resources
            ext.println("(1/2) Reading resources...")
            val fileNames = mutableSetOf<String>()
            val fileValuesMap = LinkedHashMultimap.create<String, String>()
            Files.walk(Paths.get(project.projectDir.resolve(ext.resDir).absolutePath))
                    .filter { Files.isRegularFile(it) }
                    .map { it.toFile() }
                    .filter { it.name != ".DS_Store" && !ext.ignoreList.contains(it.name) }
                    .forEach { file ->
                        ext.println(0, file.name)
                        fileNames.add(file.name)

                        ext.println(1, "${file.extension} -> ${file.name} ")
                        fileValuesMap.put(file.extension, file.name)

                        if (file.extension == "properties") {
                            val stream = file.inputStream()
                            val properties = Properties().apply { load(stream) }
                            stream.close()
                            properties.keys.map { it as? String ?: it.toString() }.forEach {
                                ext.println(2, "${file.nameWithoutExtension} -> $it")
                                fileValuesMap.put(file.nameWithoutExtension, it)
                            }
                        }
                    }

            // handle internationalization
            ext.println()
            ext.println("(2/3) Handling internationalization...")
            val resourceBundles = fileValuesMap.keySet().filterInternationalizedProperties()
            val internationalizedMap = LinkedHashMultimap.create<String, String>()
            resourceBundles.distinctInternationalizedPropertiesIdentifier().forEach { key ->
                internationalizedMap.putAll(key, resourceBundles.filter { it.startsWith(key) })
            }
            internationalizedMap.keySet().forEach { key ->
                ext.println(0, key)
                val temp = mutableSetOf<String>()
                internationalizedMap.get(key).forEach { value ->
                    val toBeRemoveds = fileValuesMap.keySet().filter { it == value }
                    toBeRemoveds.forEach { file ->
                        ext.println(1, "removing $file")
                        temp.addAll(fileValuesMap.get(file))
                        fileValuesMap.removeAll(file)
                    }
                }
                temp.forEach {
                    ext.println(1, "$key -> $it")
                    fileValuesMap.put(key, it)
                }
            }

            // class generation
            ext.println()
            ext.println("(3/3) Writing '${ext.className}.java'...")
            val outputDir = project.projectDir.resolve(ext.srcDir)
            project.tasks.create("rsync").apply {
                val oldPath = Paths.get(outputDir.absolutePath, *ext.packageName.split('.').toTypedArray(), "${ext.className}.java")
                inputs.file(oldPath.toFile())
                doFirst { Files.deleteIfExists(oldPath) }
                outputs.dir(outputDir)
                doLast { generateClass(ext, fileNames, fileValuesMap, outputDir) }
            }
        }
    }

    companion object {
        private fun generateClass(ext: RSyncExtension, fileNames: Set<String>, map: Multimap<String, String>, outputDir: File) {
            val commentBuilder = StringBuilder("rsync generated this class at ${LocalDateTime.now()} from:").appendln()
            fileNames.forEachIndexed { i, s ->
                when (i) {
                    fileNames.size - 1 -> commentBuilder.append(s)
                    else -> commentBuilder.appendln(s)
                }
            }

            JavaFile.builder(ext.packageName, TypeSpec.classBuilder(ext.className)
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
                                        map.get(innerClassName).forEach { value ->
                                            val fieldBuilder = FieldSpec.builder(String::class.java, value.substringBefore('.'), PUBLIC, STATIC, FINAL)
                                            when (value.contains('.') && ext.leadingSlash) {
                                                true -> fieldBuilder.initializer("\"/\$L\"", value)
                                                else -> fieldBuilder.initializer("\$S", value)
                                            }
                                            addField(fieldBuilder.build())
                                        }
                                    }
                                    .build())
                        }
                    }
                    .build())
                    .addFileComment(commentBuilder.toString())
                    .build()
                    .writeTo(outputDir)
        }

        private fun RSyncExtension.println(tabs: Int, message: Any?) {
            if (debug) kotlin.io.println("${StringBuilder().apply { for (i in 0 until tabs) append("  ") }}|_$message")
        }

        private fun RSyncExtension.println(message: Any?) {
            if (debug) kotlin.io.println(message)
        }

        private fun RSyncExtension.println() {
            if (debug) kotlin.io.println()
        }
    }
}