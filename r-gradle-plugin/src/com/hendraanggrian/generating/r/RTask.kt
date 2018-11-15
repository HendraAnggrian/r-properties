package com.hendraanggrian.generating.r

import com.hendraanggrian.generating.r.configuration.CSSConfiguration
import com.hendraanggrian.generating.r.configuration.ConfigurationDsl
import com.hendraanggrian.generating.r.configuration.CustomConfiguration
import com.hendraanggrian.generating.r.configuration.JSONConfiguration
import com.hendraanggrian.generating.r.configuration.PropertiesConfiguration
import com.hendraanggrian.generating.r.reader.CustomReader
import com.hendraanggrian.generating.r.reader.DefaultReader
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

    /** Actual property of [isLowercase] since annotating [isLowercase] will result in Gradle warning. */
    private var lowercase: Boolean = false

    @Internal @JvmField internal val css: CSSConfiguration = CSSConfiguration()
    @Internal @JvmField internal val properties: PropertiesConfiguration = PropertiesConfiguration()
    @Internal @JvmField internal val json: JSONConfiguration = JSONConfiguration()
    @Internal @JvmField internal val custom: CustomConfiguration = CustomConfiguration()

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

    fun css(configure: (@ConfigurationDsl CSSConfiguration).() -> Unit) = css.configure()

    fun properties(configure: (@ConfigurationDsl PropertiesConfiguration).() -> Unit) = properties.configure()

    fun json(configure: (@ConfigurationDsl JSONConfiguration).() -> Unit) = json.configure()

    fun custom(configure: (task: RTask, typeBuilder: TypeSpec.Builder, file: File) -> String?) {
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
        when {
            custom.action == null -> Reader.ALL
            else -> Reader.ALL + CustomReader(custom.action!!)
        }.processDir(rClassBuilder, root)

        logger.log(LogLevel.INFO, "Writing new R")
        JavaFile.builder(packageName, rClassBuilder.build())
            .addFileComment("Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            .build()
            .writeTo(outputDir)
    }

    private fun Array<Reader<String>>.processDir(typeBuilder: TypeSpec.Builder, dir: File) {
        dir.listFiles()
            .filter { file -> file.isValid() && file.path !in exclusions.map { it.path } }
            .forEach { file ->
                when {
                    file.isDirectory -> {
                        val innerTypeBuilder = newTypeBuilder(name(file.name))
                        processDir(innerTypeBuilder, file)
                        typeBuilder.addType(innerTypeBuilder.build())
                    }
                    file.isFile -> {
                        val prefixes = Reader.ALL.mapNotNull { it.read(this@RTask, typeBuilder, file) }
                        when {
                            prefixes.isNotEmpty() -> DefaultReader(prefixes.first())
                            else -> Reader.DEFAULT
                        }.read(this@RTask, typeBuilder, file)
                    }
                }
            }
    }

    @Internal
    fun name(name: String): String = (if (isLowercase) name.toLowerCase() else name)
        .normalizeSymbols()
        .replace("\\s+".toRegex(), " ")
        .trim()
}