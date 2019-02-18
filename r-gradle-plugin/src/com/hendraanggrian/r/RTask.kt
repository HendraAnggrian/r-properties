package com.hendraanggrian.r

import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.javapoet.buildJavaFile
import com.hendraanggrian.r.adapters.Adapter
import com.hendraanggrian.r.adapters.CssAdapter
import com.hendraanggrian.r.adapters.DefaultAdapter
import com.hendraanggrian.r.adapters.JsonAdapter
import com.hendraanggrian.r.adapters.PropertiesAdapter
import com.hendraanggrian.r.options.CssOptions
import com.hendraanggrian.r.options.JsonOptions
import com.hendraanggrian.r.options.PropertiesOptions
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
    @InputDirectory lateinit var resourcesDir: File

    /** Convenient method to set resources directory from file path, relative to project directory. */
    var resourcesDirectory: String
        @Input get() = resourcesDir.absolutePath
        set(value) {
            resourcesDir = project.projectDir.resolve(value)
        }

    /**
     * Collection of files (or directories) that are ignored from this task.
     * Default is empty.
     */
    @InputFiles var exclusions: Iterable<File> = emptyList()

    /** Convenient method to set exclusions from file path, relative to project directory. */
    fun exclude(vararg exclusions: String) {
        this.exclusions = exclusions.map { project.projectDir.resolve(it) }
    }

    /** Path that R class will be generated to. */
    @OutputDirectory lateinit var outputDir: File

    /** Convenient method to set output directory from file path, relative to project directory. */
    var outputDirectory: String
        @OutputDirectory get() = outputDir.absolutePath
        set(value) {
            outputDir = project.projectDir.resolve(value)
        }

    private var css: CssOptions? = null
    private var properties: PropertiesOptions? = null
    private var json: JsonOptions? = null

    /** Customize CSS files options with Kotlin DSL. */
    @JvmOverloads
    fun useCss(action: (Action<CssOptions>)? = null) {
        var config = css
        if (config == null) {
            config = CssOptions()
            css = config
        }
        action?.invoke(config)
    }

    /** Customize properties files options with Kotlin DSL. */
    @JvmOverloads
    fun useProperties(action: (Action<PropertiesOptions>)? = null) {
        var config = properties
        if (config == null) {
            config = PropertiesOptions()
            properties = config
        }
        action?.invoke(config)
    }

    /** Customize json files options with Kotlin DSL. */
    @JvmOverloads
    fun useJson(action: (Action<JsonOptions>)? = null) {
        var config = json
        if (config == null) {
            config = JsonOptions()
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
        require(resourcesDir.exists() && resourcesDir.isDirectory) { "Resources folder not found" }

        logger.log(LogLevel.INFO, "Deleting old $className")
        val outputDir = outputDir
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        logger.log(LogLevel.INFO, "Reading resources")
        val javaFile = buildJavaFile(packageName) {
            comment("Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            type(className) {
                modifiers = public + final
                constructor {
                    modifiers = private
                }
                processDir(
                    listOfNotNull(
                        css?.let { CssAdapter(it) },
                        json?.let { JsonAdapter(it) },
                        properties?.let { PropertiesAdapter(it) }
                    ).toTypedArray(),
                    DefaultAdapter(resourcesDir.path),
                    DefaultAdapter(resourcesDir.path, true),
                    resourcesDir
                )
            }
        }

        logger.log(LogLevel.INFO, "Writing new $className")
        javaFile.writeTo(outputDir)
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
                            modifiers = public + static + final
                            constructor {
                                modifiers = private
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