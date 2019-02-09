package com.hendraanggrian.generating.r

import com.hendraanggrian.generating.r.adapters.Adapter
import com.hendraanggrian.generating.r.adapters.CssAdapter
import com.hendraanggrian.generating.r.adapters.DefaultAdapter
import com.hendraanggrian.generating.r.adapters.JsonAdapter
import com.hendraanggrian.generating.r.adapters.PropertiesAdapter
import com.hendraanggrian.generating.r.configuration.CssConfiguration
import com.hendraanggrian.generating.r.configuration.JsonConfiguration
import com.hendraanggrian.generating.r.configuration.PropertiesConfiguration
import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.javapoet.buildJavaFile
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.invoke
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import javax.lang.model.element.Modifier
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC

/** R class generation task. */
open class RTask : DefaultTask() {

    /**
     * Package name of which R class will be generated to.
     * Default is project group, may be modified but cannot be null.
     */
    @Input var packageName: String = ""

    /**
     * Class name of R.
     * Default is `R`, may be modified but cannot be null.
     */
    @Input var className: String = "R"

    /**
     * Main resources directory.
     * Default is resources folder in main module.
     */
    @InputDirectory lateinit var resourcesDirectory: File

    /**
     * Collection of files (or directories) that are ignored from this task.
     * Default is empty.
     */
    @InputFiles val exclusions: MutableCollection<File> = mutableSetOf()

    /** Exclude certain files and directories from generated R class. */
    @InputFiles
    fun exclude(vararg files: File) {
        exclusions += files.map { resourcesDirectory.resolve(it) }
    }

    /** Exclude certain files and directories from generated R class. */
    @InputFiles
    fun exclude(vararg files: String) {
        exclusions += files.map { resourcesDirectory.resolve(it) }
    }

    /** Path that R class will be generated to. */
    @OutputDirectory lateinit var outputDirectory: File

    private var css: CssConfiguration? = null
    private var properties: PropertiesConfiguration? = null
    private var json: JsonConfiguration? = null

    /** Customize CSS files configuration with Kotlin DSL. */
    @JvmOverloads
    fun useCss(action: (Action<CssConfiguration>)? = null) {
        var config = css
        if (config == null) {
            config = CssConfiguration()
            css = config
        }
        action?.invoke(config)
    }

    /** Customize properties files configuration with Kotlin DSL. */
    @JvmOverloads
    fun useProperties(action: (Action<PropertiesConfiguration>)? = null) {
        var config = properties
        if (config == null) {
            config = PropertiesConfiguration()
            properties = config
        }
        action?.invoke(config)
    }

    /** Customize json files configuration with Kotlin DSL. */
    @JvmOverloads
    fun useJson(action: (Action<JsonConfiguration>)? = null) {
        var config = json
        if (config == null) {
            config = JsonConfiguration()
            json = config
        }
        action?.invoke(config)
    }

    /** Generate R class given provided options. */
    @TaskAction
    @Throws(IOException::class)
    @Suppress("unused")
    fun generate() {
        logger.log(LogLevel.INFO, "Checking requirements")
        require(packageName.isNotBlank()) { "Package name cannot be null" }
        require(className.isNotBlank()) { "Class name cannot be null" }
        val resourcesDir = project.projectDir.resolve(resourcesDirectory)
        require(resourcesDir.exists() && resourcesDir.isDirectory) { "Resources folder not found" }

        logger.log(LogLevel.INFO, "Deleting old $className")
        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()

        val javaFile = buildJavaFile(packageName) {
            comment("Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            type(className) {
                modifiers(PUBLIC, FINAL)
                constructor {
                    modifiers(PRIVATE)
                }
                logger.log(LogLevel.INFO, "Reading resources")
                processDir(
                    listOfNotNull(
                        css?.let { CssAdapter(it) },
                        json?.let { JsonAdapter(it) },
                        properties?.let { PropertiesAdapter(it) }
                    ).toTypedArray(),
                    DefaultAdapter(resourcesDirectory.path),
                    DefaultAdapter(resourcesDirectory.path, true),
                    resourcesDir
                )
            }
        }
        logger.log(LogLevel.INFO, "Writing new $className")
        javaFile.writeTo(outputDirectory)
    }

    private fun TypeSpecBuilder.processDir(
        optionalAdapters: Array<Adapter>,
        defaultAdapter: Adapter,
        prefixedAdapter: Adapter,
        resourcesDir: File
    ) {
        resourcesDir.listFiles()
            .filter { file -> file.isValid() && file.path !in exclusions.map { it.path } }
            .forEach { file ->
                when {
                    file.isDirectory -> {
                        type(file.name.normalize()) {
                            modifiers(Modifier.PUBLIC, Modifier.STATIC, FINAL)
                            constructor {
                                modifiers(Modifier.PRIVATE)
                            }
                            processDir(
                                optionalAdapters,
                                defaultAdapter,
                                prefixedAdapter,
                                file
                            )
                        }
                    }
                    file.isFile -> {
                        val prefixes = optionalAdapters.map { it.adapt(file, this) }
                        when {
                            prefixes.any { it } -> prefixedAdapter
                            else -> defaultAdapter
                        }.adapt(file, this)
                    }
                }
            }
    }
}