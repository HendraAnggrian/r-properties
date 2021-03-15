@file:Suppress("NOTHING_TO_INLINE", "UnstableApiUsage")

package io.github.hendraanggrian.r

import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.javapoet.buildJavaFile
import io.github.hendraanggrian.r.adapters.BaseAdapter
import io.github.hendraanggrian.r.adapters.CssAdapter
import io.github.hendraanggrian.r.adapters.JsonAdapter
import io.github.hendraanggrian.r.adapters.PathAdapter
import io.github.hendraanggrian.r.adapters.PropertiesAdapter
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import javax.lang.model.element.Modifier

/** A task that writes `R` class. */
open class RTask : DefaultTask() {

    /**
     * Package name of which `R` class will be generated to, cannot be empty.
     * If left null, project group will be assigned as value.
     */
    @Input
    val packageName: Property<String> = project.objects.property<String>()
        .convention(project.group.toString())

    /**
     * Generated class name, cannot be empty.
     * Default value is `R`.
     */
    @Input
    val className: Property<String> = project.objects.property<String>()
        .convention("R")

    /**
     * When activated, automatically make all field names uppercase.
     * It is disabled by default.
     */
    @Input
    val shouldUppercaseField: Property<Boolean> = project.objects.property<Boolean>()
        .convention(false)

    /**
     * When activated, automatically make all class names lowercase.
     * It is disabled by default.
     */
    @Input
    val shouldLowercaseClass: Property<Boolean> = project.objects.property<Boolean>()
        .convention(false)

    /**
     * Main resources directory.
     * Default is resources folder in main module.
     */
    @InputDirectory
    val resourcesDirectory: Property<File> = project.objects.property<File>()
        .convention(project.projectDir.resolve("src/main/resources"))

    /**
     * Collection of files (or directories) that are ignored from this task.
     * Default is empty.
     */
    @InputFiles
    val exclusions: SetProperty<File> = project.objects.setProperty<File>()
        .convention(emptySet())

    /** Convenient method to set exclusions relative to project directory. */
    fun exclude(vararg exclusions: String) {
        this.exclusions.set(exclusions.map { project.projectDir.resolve(it) })
    }

    /**
     * Directory of which `R` class will be generated to.
     * Default is `build/generated/r` relative to project directory.
     */
    @OutputDirectory
    val outputDirectory: Property<File> = project.objects.property<File>()
        .convention(project.buildDir.resolve("generated/r"))

    private var cssSettings: CssSettings? = null
    private var propertiesSettings: PropertiesSettings? = null
    private var jsonSettings: JsonSettings? = null

    init {
        outputs.upToDateWhen { false } // always consider this task out of date
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
    inline fun css(noinline configuration: CssSettings.() -> Unit): Unit =
        configureCss(configuration)

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
    inline fun properties(noinline configuration: PropertiesSettings.() -> Unit): Unit =
        configureProperties(configuration)

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
    inline fun json(noinline configuration: JsonSettings.() -> Unit): Unit =
        configureJson(configuration)

    /** Generate R class given provided options. */
    @TaskAction
    fun generate() {
        logger.info("Generating R:")

        require(packageName.get().isNotBlank()) { "Package name cannot be empty" }
        require(className.get().isNotBlank()) { "Class name cannot be empty" }
        require(resourcesDirectory.get().exists() && resourcesDirectory.get().isDirectory) { "Resources folder not found" }

        if (outputDirectory.get().exists()) {
            logger.info("  Existing source deleted")
            outputDirectory.get().deleteRecursively()
        }
        outputDirectory.get().mkdirs()

        val javaFile = buildJavaFile(packageName.get()) {
            comment = "Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}"
            addClass(className.get()) {
                addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                methods.addConstructor { addModifiers(Modifier.PRIVATE) }
                processDir(
                    listOfNotNull(
                        cssSettings?.let { CssAdapter(it, shouldUppercaseField.get(), logger) },
                        jsonSettings?.let { JsonAdapter(it, shouldUppercaseField.get(), logger) },
                        propertiesSettings?.let {
                            PropertiesAdapter(it, shouldLowercaseClass.get(), shouldUppercaseField.get(), logger)
                        }
                    ),
                    PathAdapter(resourcesDirectory.get().path, shouldUppercaseField.get(), logger),
                    resourcesDirectory.get()
                )
            }
        }

        javaFile.writeTo(outputSrcDir)
        logger.info("  Source generated")
    }

    internal val outputSrcDir: File
        @Internal get() = outputDirectory.get().resolve("src/main")

    internal val outputClassesDir: File
        @Internal get() = outputDirectory.get().resolve("classes/main")

    private fun TypeSpecBuilder.processDir(
        adapters: Iterable<BaseAdapter>,
        pathAdapter: PathAdapter,
        resourcesDir: File
    ) {
        val exclusionPaths = exclusions.get().map { it.path }
        resourcesDir.listFiles()!!
            .filter { file -> !file.isHidden && file.path !in exclusionPaths }
            .forEach { file ->
                when {
                    file.isDirectory -> {
                        var innerClassName = file.name.toJavaNameOrNull()
                        if (innerClassName != null) {
                            if (shouldLowercaseClass.get()) {
                                innerClassName = innerClassName.toLowerCase()
                            }
                            types.addClass(innerClassName) {
                                addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                                methods.addConstructor { addModifiers(Modifier.PRIVATE) }
                                processDir(adapters, pathAdapter, file)
                            }
                        }
                    }
                    file.isFile -> {
                        pathAdapter.isUnderscorePrefix = adapters.any { it.process(this, file) }
                        pathAdapter.process(this, file)
                    }
                }
            }
    }
}
