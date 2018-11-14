@file:Suppress("UnusedImport")

package com.hendraanggrian.generating.r

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.provideDelegate // ktlint-disable
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like R class with this plugin. */
class RPlugin : Plugin<Project> {

    companion object {
        const val CLASS_NAME = "R"
        const val GROUP_NAME = "generating"
        const val GENERATED_DIRECTORY = "generated"
    }

    override fun apply(project: Project) {
        val generateR by project.tasks.registering(RTask::class) {
            group = GROUP_NAME
            resourcesDir = project.projectDir.resolve("src/main/resources")
            outputDir = project.buildDir.resolve("$GENERATED_DIRECTORY/r/src/main")
        }
        // project group will return correct name after evaluated
        project.afterEvaluate {
            generateR {
                if (packageName.isEmpty()) {
                    packageName = project.group.toString()
                }
            }
        }
        val compileR by project.tasks.registering(JavaCompile::class) {
            dependsOn(generateR.get())
            group = GROUP_NAME
            classpath = project.files()
            destinationDir = project.buildDir.resolve("$GENERATED_DIRECTORY/r/classes/main")

            val generateRTask = generateR.get()
            dependsOn(generateRTask)
            source(generateRTask.outputDir)
        }

        val compileRTask = compileR.get()
        val compiledClasses = project.files(compileRTask.outputs.files.filter { !it.name.endsWith("dependency-cache") })
        compiledClasses.builtBy(compileRTask)
        project.convention.getPlugin<JavaPluginConvention>().sourceSets {
            "main" {
                compileClasspath += compiledClasses
                compiledClasses.forEach { output.dir(it) }
            }
        }

        require(project.plugins.hasPlugin("org.gradle.idea")) { "Plugin 'idea' must be applied." }

        val providedR by project.configurations.registering
        providedR {
            dependencies += project.dependencies.create(compiledClasses)
            project.extensions
                .getByName<IdeaModel>("idea")
                .module
                .scopes["PROVIDED"]!!["plus"]!! += this
        }
    }
}