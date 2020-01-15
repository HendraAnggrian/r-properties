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

/** A task that writes `R` class. */
open class RTask : DefaultTask() {

    /**
     * Package name of which `R` class will be generated to, cannot be empty.
     * If left empty or unmodified, project group will be assigned as value.
     */
    @Input var packageName: String = ""

    /**
     * Generated class name, cannot be empty.
     * Default value is `R`.
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

    /** Convenient method to set resources directory relative to project directory. */
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

    /** Convenient method to set exclusions relative to project directory. */
    fun exclude(vararg exclusions: String) {
        this.exclusions = exclusions.map { project.projectDir.resolve(it) }
    }

    /**
     * Directory of which `R` class will be generated to.
     * Default is `build/generated` relative to project directory.
     */
    @OutputDirectory lateinit var outputDir: File

    /** Convenient method to set output directory relative to project directory. */
    var outputDirectory: String
        @Input get() = outputDir.absolutePath
        set(value) {
            outputDir = project.projectDir.resolve(value)
        }

    private var cssSettings: CssSettings? = null
    private var propertiesSettings: PropertiesSettings? = null
    private var jsonSettings: JsonSettings? = null

    init {
        // always consider this task out of date
        outputs.upToDateWhen { false }
    }

    /** Enable CSS files support with default configuration. */
    fun configureCss() {
        var settings = cssSettings
        if (settings == null) {
            settings = CssSettings()
            cssSettings = settings
        }
    }

    /** Enable CSS files support with customized [configuration]. */
    fun configureCss(configuration: Action<CssSettings>) {
        configureCss()
        configuration(cssSettings!!)
    }

    /** Enable CSS files support with customized [configuration] in Kotlin DSL. */
    inline fun css(noinline configuration: CssSettings.() -> Unit) = configureCss(configuration)

    /** Enable properties files support with default configuration. */
    fun configureProperties() {
        var settings = propertiesSettings
        if (settings == null) {
            settings = PropertiesSettings()
            propertiesSettings = settings
        }
    }

    /** Enable properties files support with customized [configuration]. */
    fun configureProperties(configuration: Action<PropertiesSettings>) {
        configureProperties()
        configuration(propertiesSettings!!)
    }

    /** Enable properties files support with customized [configuration] in Kotlin DSL. */
    inline fun properties(noinline configuration: PropertiesSettings.() -> Unit) = configureProperties(configuration)

    /** Enable json files support with default configuration. */
    fun configureJson() {
        var settings = jsonSettings
        if (settings == null) {
            settings = JsonSettings()
            jsonSettings = settings
        }
    }

    /** Enable json files support with customized [configuration]. */
    fun configureJson(configuration: Action<JsonSettings>) {
        configureJson()
        configuration(jsonSettings!!)
    }

    /** Enable json files support with customized [configuration] in Kotlin DSL. */
    inline fun json(noinline configuration: JsonSettings.() -> Unit) = configureJson(configuration)

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
                        cssSettings?.let { CssAdapter(uppercaseFieldName, it) },
                        jsonSettings?.let { JsonAdapter(uppercaseFieldName, it) },
                        propertiesSettings?.let { PropertiesAdapter(uppercaseFieldName, it) }
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
            .filter { file -> !file.isHidden && file.path !in exclusionPaths }
            .forEach { file ->
                when {
                    file.isDirectory -> file.name.toFieldNameOrNull()?.let {
                        types.addClass(it) {
                            addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            methods.addConstructor {
                                addModifiers(Modifier.PRIVATE)
                            }
                            processDir(adapters, defaultAdapter, prefixedAdapter, file)
                        }
                    }
                    file.isFile -> {
                        val prefixes = adapters.map { it.run { process(file) } }
                        when {
                            prefixes.any { it } -> prefixedAdapter
                            else -> defaultAdapter
                        }.run { process(file) }
                    }
                }
            }
    }
}
