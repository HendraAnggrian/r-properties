package com.hendraanggrian.generating.r

import com.hendraanggrian.generating.r.readers.CssReader
import com.hendraanggrian.generating.r.readers.PropertiesReader
import com.hendraanggrian.generating.r.readers.Reader
import com.hendraanggrian.generating.r.readers.ResourceBundlesReader
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import java.util.Properties
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC

/** R class generation task. */
open class RTask : DefaultTask() {

    /** Actual property of [isLowercase] since annotating [isLowercase] will result in Gradle warning. */
    private var lowercase: Boolean = false

    /**
     * Package name of which R class will be generated to.
     * Default is project group.
     */
    @Input var packageName: String = ""

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
    @InputFiles val exclusions: MutableCollection<File> = mutableSetOf()

    /** Exclude certain files and directories from generated R class. */
    @InputFiles fun exclude(vararg files: File) {
        exclusions += files.map { resourcesDir.resolve(it) }
    }

    /** Exclude certain files and directories from generated R class. */
    @InputFiles fun exclude(vararg files: String) {
        exclusions += files.map { resourcesDir.resolve(it) }
    }

    /** Path that R class will be generated to. */
    @OutputDirectory lateinit var outputDir: File

    @Input var readProperties: Boolean = true

    @Input var readCssStyles: Boolean = true

    /** Generate R class given provided options. */
    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        val root = project.projectDir.resolve(resourcesDir)
        requireNotNull(root) { "Resources folder not found" }

        logger.log(LogLevel.INFO, "Deleting old R")
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val rClassBuilder = TypeSpec.classBuilder(RPlugin.CLASS_NAME)
            .addModifiers(PUBLIC, FINAL)
            .addMethod(privateConstructor())
        processDir(rClassBuilder, root) { if (isLowercase) toLowerCase() else this }
        JavaFile.builder(packageName, rClassBuilder.build())
            .addFileComment("Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            .build()
            .writeTo(outputDir)
    }

    private fun processDir(typeBuilder: TypeSpec.Builder, dir: File, convert: String.() -> String) {
        dir.listFiles().forEach {
            when {
                it.isDirectory -> {
                    val innerTypeBuilder = newInnerTypeBuilder(it.name.convert())
                    processDir(innerTypeBuilder, it, convert)
                    typeBuilder.addType(innerTypeBuilder.build())
                }
                it.isFile -> {
                    when {
                        readProperties && it.extension == "properties" -> when {
                            it.isResourceBundle() -> ResourceBundlesReader.read(typeBuilder, it, convert)
                            else -> PropertiesReader.read(typeBuilder, it, convert)
                        }
                        readCssStyles && it.extension == "css" -> CssReader.read(typeBuilder, it, convert)
                        else -> Reader.read(typeBuilder, it, convert)
                    }
                }
            }
        }
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
    }
}