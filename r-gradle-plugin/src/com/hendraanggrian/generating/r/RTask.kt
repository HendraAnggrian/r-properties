package com.hendraanggrian.generating.r

import com.hendraanggrian.generating.r.configuration.CSSConfiguration
import com.hendraanggrian.generating.r.configuration.ConfigurationDsl
import com.hendraanggrian.generating.r.configuration.CustomConfiguration
import com.hendraanggrian.generating.r.configuration.JSONConfiguration
import com.hendraanggrian.generating.r.configuration.PropertiesConfiguration
import com.hendraanggrian.generating.r.reader.CSSReader
import com.hendraanggrian.generating.r.reader.CustomReader
import com.hendraanggrian.generating.r.reader.DefaultReader
import com.hendraanggrian.generating.r.reader.JSONReader
import com.hendraanggrian.generating.r.reader.PropertiesReader
import com.hendraanggrian.generating.r.reader.Reader
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
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
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC

/** R class generation task. */
open class RTask : DefaultTask() {

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

    @Internal @JvmField internal val css: CSSConfiguration = CSSConfiguration()
    @Internal @JvmField internal val properties: PropertiesConfiguration = PropertiesConfiguration()
    @Internal @JvmField internal val json: JSONConfiguration = JSONConfiguration()
    @Internal @JvmField internal val custom: CustomConfiguration = CustomConfiguration()

    fun css(configure: (@ConfigurationDsl CSSConfiguration).() -> Unit) = css.configure()

    fun properties(configure: (@ConfigurationDsl PropertiesConfiguration).() -> Unit) = properties.configure()

    fun json(configure: (@ConfigurationDsl JSONConfiguration).() -> Unit) = json.configure()

    fun custom(configure: (typeBuilder: TypeSpec.Builder, file: File) -> Boolean) {
        custom.action = configure
    }

    /** Generate R class given provided options. */
    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        val root = project.projectDir.resolve(resourcesDir)
        requireNotNull(root) { "Resources folder not found" }

        logger.log(LogLevel.INFO, "Deleting old R")
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val rClassBuilder = TypeSpec.classBuilder("R")
            .addModifiers(PUBLIC, FINAL)
            .addMethod(privateConstructor())

        logger.log(LogLevel.INFO, "Reading resources")
        val readers = arrayOf(CSSReader(css), JSONReader(json), PropertiesReader(properties))
        processDir(
            custom.action?.let { readers + CustomReader(it) } ?: readers,
            DefaultReader(resourcesDir.path),
            DefaultReader(resourcesDir.path, true),
            rClassBuilder,
            root
        )

        logger.log(LogLevel.INFO, "Writing new R")
        JavaFile.builder(packageName, rClassBuilder.build())
            .addFileComment("Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            .build()
            .writeTo(outputDir)
    }

    private fun processDir(
        readers: Array<Reader>,
        defaultReader: Reader,
        prefixedReader: Reader,
        typeBuilder: TypeSpec.Builder,
        dir: File
    ) {
        dir.listFiles()
            .filter { file -> file.isValid() && file.path !in exclusions.map { it.path } }
            .forEach { file ->
                when {
                    file.isDirectory -> {
                        val innerTypeBuilder = newTypeBuilder(file.name)
                        processDir(readers, defaultReader, prefixedReader, innerTypeBuilder, file)
                        typeBuilder.addType(innerTypeBuilder.build())
                    }
                    file.isFile -> {
                        val prefixes = readers.map { it.read(typeBuilder, file) }
                        when {
                            prefixes.any { it } -> prefixedReader
                            else -> defaultReader
                        }.read(typeBuilder, file)
                    }
                }
            }
    }

    /** Only to be used within [custom] DSL. */
    @Internal fun TypeSpec.Builder.addField(name: String, value: String) = addStringField(name, value)
}