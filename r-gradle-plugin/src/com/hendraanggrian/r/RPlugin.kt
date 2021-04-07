package com.hendraanggrian.r

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like `R` class with this plugin. */
class RPlugin : Plugin<Project> {

    companion object {
        const val GROUP_NAME = "code generation"
    }

    override fun apply(project: Project) {
        require(project.plugins.hasPlugin("org.gradle.idea")) { "Plugin 'idea' must be applied" }

        val generateR by project.tasks.registering(RTask::class) {
            group = GROUP_NAME
            description = "Generate Android-like R class."
        }
        val generateRTask by generateR

        val compileR by project.tasks.registering(JavaCompile::class) {
            dependsOn(generateRTask)
            description = "Compiles R source file."
            group = GROUP_NAME
            classpath = project.files()
            destinationDir = generateRTask.outputClassesDir
            source(generateRTask.outputSrcDir)
        }
        val compileRTask by compileR
        val compiledClasses = project
            .files(compileRTask.outputs.files.filter { !it.name.endsWith("dependency-cache") })
            .builtBy(compileRTask)

        project.convention.getPlugin<JavaPluginConvention>().sourceSets {
            "main" {
                compileClasspath += compiledClasses
                compiledClasses.forEach { output.dir(it) }
            }
        }

        val providedR by project.configurations.registering {
            dependencies += project.dependencies.create(compiledClasses)
        }
        val providedRConfig by providedR

        project.extensions
            .getByName<IdeaModel>("idea")
            .module
            .scopes["PROVIDED"]!!["plus"]!! += providedRConfig
    }
}
