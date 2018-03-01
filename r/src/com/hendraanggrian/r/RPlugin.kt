package com.hendraanggrian.r

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like R class with this plugin. */
class RPlugin : Plugin<Project> {

    private lateinit var project: Project

    override fun apply(target: Project) {
        project = target

        val generateTask = createGenerateTask()
        val compileTask = generateTask.createCompileTask()
        val compiledClasses = project.files(compileTask.outputs.files.filter { !it.name.endsWith("dependency-cache") })
        compiledClasses.builtBy(compileTask)

        val sourceSet = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets["main"]
        sourceSet.compileClasspath += compiledClasses
        compiledClasses.forEach { sourceSet.output.dir(it) }

        require(project.plugins.hasPlugin("org.gradle.idea")) { "plugin 'idea' must be applied" }
        val providedConfig = project.configurations.create("provided$CLASS_NAME")
        providedConfig.dependencies += project.dependencies.create(compiledClasses)
        (project.extensions["idea"] as IdeaModel).module.scopes["PROVIDED"]!!["plus"]!! += providedConfig
    }

    private fun createGenerateTask(): RTask = project.task(
        mapOf("type" to RTask::class.java, "group" to GROUP_NAME),
        "generate$CLASS_NAME") as RTask

    private fun RTask.createCompileTask(): JavaCompile = project.task(
        mapOf("type" to JavaCompile::class.java, "group" to GROUP_NAME, "dependsOn" to this),
        "compile$CLASS_NAME",
        closureOf<JavaCompile> {
            classpath = project.files()
            destinationDir = project.buildDir.resolve("$GENERATED_DIRECTORY/$EXTENSION_NAME/classes/main")
            source(outputDir)
        }) as JavaCompile

    companion object {
        internal const val EXTENSION_NAME = "r"
        internal const val CLASS_NAME = "R"
        internal const val GROUP_NAME = "generation"
        internal const val GENERATED_DIRECTORY = "generated"
    }
}