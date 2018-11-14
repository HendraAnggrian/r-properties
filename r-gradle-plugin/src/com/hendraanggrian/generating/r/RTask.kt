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

    /** Set false to skip reading keys of properties files. */
    @Input var readProperties: Boolean = true

    /** Set false to skip reading style class of css files. */
    @Input var readCss: Boolean = true

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
        processDir(rClassBuilder, root)
        JavaFile.builder(packageName, rClassBuilder.build())
            .addFileComment("Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            .build()
            .writeTo(outputDir)
    }

    private fun processDir(typeBuilder: TypeSpec.Builder, dir: File) {
        dir.listFiles()
            .filter { file -> file.isValid() && file.path !in exclusions.map { it.path } }
            .forEach {
                when {
                    it.isDirectory -> {
                        val innerTypeBuilder = newTypeBuilder(name(it.name))
                        processDir(innerTypeBuilder, it)
                        typeBuilder.addType(innerTypeBuilder.build())
                    }
                    it.isFile -> {
                        when {
                            readProperties && it.extension == "properties" -> when {
                                it.isResourceBundle() -> ResourceBundlesReader.read(this, typeBuilder, it)
                                else -> PropertiesReader.read(this, typeBuilder, it)
                            }
                            readCss && it.extension == "css" -> CssReader.read(this, typeBuilder, it)
                            else -> Reader.read(this, typeBuilder, it)
                        }
                    }
                }
            }
    }

    internal fun name(name: String): String = (if (isLowercase) name.toLowerCase() else name).normalizeSymbols().trim()
}