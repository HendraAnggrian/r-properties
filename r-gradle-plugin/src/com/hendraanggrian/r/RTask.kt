@file:Suppress("NOTHING_TO_INLINE")

package com.hendraanggrian.r

import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.javapoet.buildJavaFile
import com.hendraanggrian.r.adapters.BaseAdapter
import com.hendraanggrian.r.adapters.CssAdapter
import com.hendraanggrian.r.adapters.JsonAdapter
import com.hendraanggrian.r.adapters.PathAdapter
import com.hendraanggrian.r.adapters.PropertiesAdapter
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import javax.lang.model.element.Modifier
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.invoke

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
     * When activated, automatically make all field names uppercase.
     * Default is false.
     */
    @Input var uppercaseFieldName: Boolean = false

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
        @Input get() = outputDir.absolutePath
        set(value) {
            outputDir = project.projectDir.resolve(value)
        }

    private var cssOptions: CssOptions? = null
    private var propertiesOptions: PropertiesOptions? = null
    private var jsonOptions: JsonOptions? = null

    init {
        // always consider this task out of date
        outputs.upToDateWhen { false }
    }

    /** Enable CSS files support with default configuration. */
    fun configureCss() {
        var options = cssOptions
        if (options == null) {
            options = CssOptions()
            cssOptions = options
        }
    }

    /** Enable CSS files support with customized [configuration]. */
    fun configureCss(configuration: Action<CssOptions>) {
        configureCss()
        configuration(cssOptions!!)
    }

    /** Enable CSS files support with customized [configuration] in Kotlin DSL. */
    inline fun css(noinline configuration: CssOptions.() -> Unit) = configureCss(configuration)

    /** Enable properties files support with default configuration. */
    fun configureProperties() {
        var options = propertiesOptions
        if (options == null) {
            options = PropertiesOptions()
            propertiesOptions = options
        }
    }

    /** Enable properties files support with customized [configuration]. */
    fun configureProperties(configuration: Action<PropertiesOptions>) {
        configureProperties()
        configuration(propertiesOptions!!)
    }

    /** Enable properties files support with customized [configuration] in Kotlin DSL. */
    inline fun properties(noinline configuration: PropertiesOptions.() -> Unit) = configureProperties(configuration)

    /** Enable json files support with default configuration. */
    fun configureJson() {
        var options = jsonOptions
        if (options == null) {
            options = JsonOptions()
            jsonOptions = options
        }
    }

    /** Enable json files support with customized [configuration]. */
    fun configureJson(configuration: Action<JsonOptions>) {
        configureJson()
        configuration(jsonOptions!!)
    }

    /** Enable json files support with customized [configuration] in Kotlin DSL. */
    inline fun json(noinline configuration: JsonOptions.() -> Unit) = configureJson(configuration)

    /** Generate R class given provided options. */
    @TaskAction fun generate() {
        logger.info("Checking requirements")
        require(packageName.isNotBlank()) { "Package name cannot be null" }
        require(className.isNotBlank()) { "Class name cannot be null" }
        require(resourcesDir.exists() && resourcesDir.isDirectory) { "Resources folder not found" }

        logger.info("Deleting old $className")
        val outputDir = outputDir
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        logger.info("Reading resources")
        val javaFile = buildJavaFile(packageName) {
            comment = "Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}"
            addClass(className) {
                addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                methods.addConstructor {
                    addModifiers(Modifier.PRIVATE)
                }
                processDir(
                    listOfNotNull(
                        cssOptions?.let { CssAdapter(uppercaseFieldName, it) },
                        jsonOptions?.let { JsonAdapter(uppercaseFieldName, it) },
                        propertiesOptions?.let { PropertiesAdapter(uppercaseFieldName, it) }
                    ),
                    PathAdapter(uppercaseFieldName, resourcesDir.path),
                    PathAdapter(uppercaseFieldName, resourcesDir.path, true),
                    resourcesDir
                )
            }
        }

        logger.info("Writing new $className")
        javaFile.writeTo(outputDir)
    }

    private fun TypeSpecBuilder.processDir(
        adapters: Iterable<BaseAdapter>,
        defaultAdapter: BaseAdapter,
        prefixedAdapter: BaseAdapter,
        resourcesDir: File
    ) {
        val exclusionPaths = exclusions.map { it.path }
        resourcesDir.listFiles()!!
            .filter { file -> file.isValid() && file.path !in exclusionPaths }
            .forEach { file ->
                when {
                    file.isDirectory -> file.name.toFieldName()?.let {
                        types.addClass(it) {
                            addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            methods.addConstructor {
                                addModifiers(Modifier.PRIVATE)
                            }
                            processDir(adapters, defaultAdapter, prefixedAdapter, file)
                        }
                    }
                    file.isFile -> {
                        val prefixes = adapters.map { it.run { adapt(file) } }
                        when {
                            prefixes.any { it } -> prefixedAdapter
                            else -> defaultAdapter
                        }.run { adapt(file) }
                    }
                }
            }
    }
}
