package com.hendraanggrian.r

import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.javapoet.buildJavaFile
import com.hendraanggrian.javapoet.final
import com.hendraanggrian.javapoet.private
import com.hendraanggrian.javapoet.public
import com.hendraanggrian.javapoet.static
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.invoke
import java.io.File
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
     * When activated, automatically make all field names uppercase.
     * Default is false.
     */
    @Input var uppercaseFieldName: Boolean = false

    /**
     * When activated, it will automatically fix invalid field names. Otherwise, it will skip the field.
     * Default is false.
     */
    @Input var fixFieldName: Boolean = false

    /**
     * Main resources directory.
     * Default is resources folder in main module.
     */
    @InputDirectory lateinit var resourcesDir: File

    /** Convenient method to set resources directory from file path, relative to project directory. */
    var resourcesDirectory: String
        @InputDirectory get() = resourcesDir.absolutePath
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

    private var cssOptions: CssOptions? = null
    private var propertiesOptions: PropertiesOptions? = null
    private var jsonOptions: JsonOptions? = null

    init {
        // always consider this task out of date
        outputs.upToDateWhen { false }
    }

    /** Activates CSS files support. */
    fun configureCss() {
        var options = cssOptions
        if (options == null) {
            options = CssOptions()
            cssOptions = options
        }
    }

    /** Activates CSS files support with Groovy closure. */
    fun configureCss(action: Action<CssOptions>) {
        configureCss()
        action(cssOptions!!)
    }

    /** Activates CSS files support with Kotlin DSL. */
    fun css(action: CssOptions.() -> Unit) =
        configureCss(action)

    /** Activates properties files support. */
    fun configureProperties() {
        var options = propertiesOptions
        if (options == null) {
            options = PropertiesOptions()
            propertiesOptions = options
        }
    }

    /** Activates properties files support with Groovy closure. */
    fun configureProperties(action: Action<PropertiesOptions>) {
        configureProperties()
        action(propertiesOptions!!)
    }

    /** Activates properties files support with Kotlin DSL. */
    fun properties(action: PropertiesOptions.() -> Unit) =
        configureProperties(action)

    /** Activates JSON files support. */
    fun configureJson() {
        var options = jsonOptions
        if (options == null) {
            options = JsonOptions()
            jsonOptions = options
        }
    }

    /** Activates JSON files support with Groovy closure. */
    fun configureJson(action: Action<JsonOptions>) {
        configureJson()
        action(jsonOptions!!)
    }

    /** Activates JSON files support with Kotlin DSL. */
    fun json(action: JsonOptions.() -> Unit) =
        configureJson(action)

    /** Generate R class given provided options. */
    @TaskAction
    fun generate() {
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
                addModifiers(public, final)
                methods.addConstructor {
                    addModifiers(private)
                }
                processDir(
                    listOfNotNull(
                        cssOptions?.let { CssAdapter(uppercaseFieldName, fixFieldName, it) },
                        jsonOptions?.let { JsonAdapter(uppercaseFieldName, fixFieldName, it) },
                        propertiesOptions?.let { PropertiesAdapter(uppercaseFieldName, fixFieldName, it) }
                    ),
                    DefaultAdapter(uppercaseFieldName, fixFieldName, resourcesDir.path),
                    DefaultAdapter(uppercaseFieldName, fixFieldName, resourcesDir.path, true),
                    resourcesDir
                )
            }
        }

        logger.info("Writing new $className")
        javaFile.writeTo(outputDir)
    }

    private fun TypeSpecBuilder.processDir(
        adapters: Iterable<Adapter>,
        defaultAdapter: Adapter,
        prefixedAdapter: Adapter,
        resourcesDir: File
    ): Unit = resourcesDir.listFiles()!!
        .filter { file -> file.isValid() && file.path !in exclusions.map { it.path } }
        .forEach { file ->
            when {
                file.isDirectory -> {
                    types.addClass(file.name.normalize()) {
                        addModifiers(public, static, final)
                        methods.addConstructor {
                            addModifiers(private)
                        }
                        processDir(adapters, defaultAdapter, prefixedAdapter, file)
                    }
                }
                file.isFile -> {
                    val prefixes = adapters.map { it.adapt(file, this) }
                    when {
                        prefixes.any { it } -> prefixedAdapter
                        else -> defaultAdapter
                    }.adapt(file, this)
                }
            }
        }
}
