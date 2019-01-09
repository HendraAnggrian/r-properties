@file:Suppress("UnusedImport", "unused", "UNUSED_VARIABLE")

package com.hendraanggrian.generating.r

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate // ktlint-disable
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like R class with this plugin. */
class RPlugin : Plugin<Project> {

    companion object {
        const val GROUP_NAME = "r"
    }

    override fun apply(project: Project) {
        val generateR by project.tasks.registering(RTask::class) {
            group = GROUP_NAME
            resourcesDirectory = project.projectDir.resolve("src/main/resources")
            outputDirectory = project.buildDir.resolve("generated/r/src/main")
        }
        val generateRTask by generateR
        // project group will return correct name after evaluated
        project.afterEvaluate {
            generateR {
                if (packageName.isEmpty()) {
                    packageName = project.group.toString()
                }
            }
        }

        val compileR by project.tasks.registering(JavaCompile::class) {
            dependsOn(generateRTask)
            group = GROUP_NAME
            classpath = project.files()
            destinationDir = project.buildDir.resolve("generated/r/classes/main")
            source(generateRTask.outputDirectory)
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

        require(project.plugins.hasPlugin("org.gradle.idea")) { "Plugin 'idea' must be applied" }

        val providedR by project.configurations.registering {
            dependencies += project.dependencies.create(compiledClasses)
        }
        project.extensions
            .getByName<IdeaModel>("idea")
            .module
            .scopes["PROVIDED"]!!["plus"]!! += providedR.get()
    }
}