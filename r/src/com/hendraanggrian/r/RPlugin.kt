package com.hendraanggrian.r

import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.closureOf
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like R class with this plugin. */
class RPlugin : Plugin<Project> {

    private lateinit var project: Project

    override fun apply(p: Project) {
        project = p
        val ext = project.extensions.create(EXTENSION_NAME, RExtension::class.java)
        project.afterEvaluate {
            if (ext.pkgName == null) ext.pkgName = project.group.findInClosure()

            val generateTask = ext.generateTask
            generateTask.outputDir = project.buildDir.toPath().resolve(GENERATED_SOURCE_OUTPUT).toFile()

            val compileTask = generateTask.compileTask
            val compiledClasses = project.files(compileTask.outputs.files.filter { !it.name.endsWith("dependency-cache") })
            compiledClasses.builtBy(compileTask)

            val sourceSet = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName("main")
            sourceSet.compileClasspath += compiledClasses
            compiledClasses.forEach { sourceSet.output.dir(it) }

            require(project.plugins.hasPlugin("org.gradle.idea")) { "plugin 'idea' must be applied" }
            val providedConfig = project.configurations.create("provided$CLASS_NAME")
            providedConfig.dependencies.add(project.dependencies.create(compiledClasses))
            (project.extensions.getByName("idea") as IdeaModel).module.scopes["PROVIDED"]!!["plus"]!! += providedConfig
        }
    }

    private inline val RExtension.generateTask: GenerateRTask
        get() = project.task(mapOf("type" to GenerateRTask::class.java), "generate$CLASS_NAME", closureOf<GenerateRTask> {
            packageName = pkgName!!
            resourcesDir = resDir
        }) as GenerateRTask

    private inline val GenerateRTask.compileTask: JavaCompile
        get() = project.task(mapOf("type" to JavaCompile::class.java, "dependsOn" to this), "compile$CLASS_NAME", closureOf<JavaCompile> {
            classpath = project.files()
            destinationDir = project.buildDir.toPath().resolve(GENERATED_SOURCE_CLASSES).toFile()
            source(this@compileTask.outputDir)
        }) as JavaCompile

    companion object {
        internal const val EXTENSION_NAME = "r"
        internal const val CLASS_NAME = "R"
        private const val GENERATED_SOURCE_OUTPUT = "generated/$EXTENSION_NAME/src/main"
        private const val GENERATED_SOURCE_CLASSES = "generated/$EXTENSION_NAME/classes/main"

        private fun Any.findInClosure(): String {
            var s = this
            while (s is Closure<*>) s = s.call()
            return when (s) {
                is String -> s
                else -> s.toString()
            }
        }
    }
}