package com.hendraanggrian.generating.r

import com.hendraanggrian.generating.r.configuration.ConfigurationDsl
import com.hendraanggrian.generating.r.configuration.CssConfiguration
import com.hendraanggrian.generating.r.configuration.CustomConfiguration
import com.hendraanggrian.generating.r.configuration.JsonConfiguration
import com.hendraanggrian.generating.r.configuration.PropertiesConfiguration
import com.hendraanggrian.generating.r.reader.CssReader
import com.hendraanggrian.generating.r.reader.CustomReader
import com.hendraanggrian.generating.r.reader.DefaultReader
import com.hendraanggrian.generating.r.reader.JsonReader
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
     * Class name of R.
     */
    @Input var className: String = "R"

    /**
     * Path of resources that will be read.
     * Default is resources folder in main module.
     */
    @InputDirectory lateinit var resourcesDirectory: File

    /**
     * Collection of files (or directories) that are ignored from this task.
     * Default is empty.
     */
    @InputFiles val exclusions: MutableCollection<File> = mutableSetOf()

    /** Exclude certain files and directories from generated R class. */
    @InputFiles fun exclude(vararg files: File) {
        exclusions += files.map { resourcesDirectory.resolve(it) }
    }

    /** Exclude certain files and directories from generated R class. */
    @InputFiles fun exclude(vararg files: String) {
        exclusions += files.map { resourcesDirectory.resolve(it) }
    }

    /** Path that R class will be generated to. */
    @OutputDirectory lateinit var outputDirectory: File

    @Internal @JvmField internal val css: CssConfiguration = CssConfiguration()
    @Internal @JvmField internal val properties: PropertiesConfiguration = PropertiesConfiguration()
    @Internal @JvmField internal val json: JsonConfiguration = JsonConfiguration()
    @Internal @JvmField internal val custom: CustomConfiguration = CustomConfiguration()

    /** Customize CSS files configuration with Kotlin DSL. */
    fun css(configure: (@ConfigurationDsl CssConfiguration).() -> Unit) = css.configure()

    /** Customize properties files configuration with Kotlin DSL. */
    fun properties(configure: (@ConfigurationDsl PropertiesConfiguration).() -> Unit) = properties.configure()

    /** Customize json files configuration with Kotlin DSL. */
    fun json(configure: (@ConfigurationDsl JsonConfiguration).() -> Unit) = json.configure()

    /** Customize custom action with Kotlin DSL. */
    fun custom(
        configure: (@ConfigurationDsl CustomConfiguration).(
            typeBuilder: TypeSpec.Builder,
            file: File
        ) -> Boolean
    ) {
        custom.action = { typeBuilder, file ->
            custom.configure(typeBuilder, file)
        }
    }

    /** Generate R class given provided options. */
    @TaskAction
    @Throws(IOException::class)
    @Suppress("unused")
    fun generate() {
        val root = project.projectDir.resolve(resourcesDirectory)
        requireNotNull(root) { "Resources folder not found" }

        logger.log(LogLevel.INFO, "Deleting old $className")
        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()

        val rClassBuilder = TypeSpec.classBuilder(className)
            .addModifiers(PUBLIC, FINAL)
            .addMethod(privateConstructor())

        logger.log(LogLevel.INFO, "Reading resources")
        val readers = arrayOf(CssReader(css), JsonReader(json), PropertiesReader(properties))
        processDir(
            custom.action?.let { readers + CustomReader(it) } ?: readers,
            DefaultReader(resourcesDirectory.path),
            DefaultReader(resourcesDirectory.path, true),
            rClassBuilder,
            root
        )

        logger.log(LogLevel.INFO, "Writing new $className")
        JavaFile.builder(packageName, rClassBuilder.build())
            .addFileComment("Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            .build()
            .writeTo(outputDirectory)
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
}