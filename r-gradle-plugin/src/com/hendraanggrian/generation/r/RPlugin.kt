package com.hendraanggrian.generation.r

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like R class with this plugin. */
class RPlugin : Plugin<Project> {

    private lateinit var generateR: TaskProvider<RTask>
    private lateinit var compileR: TaskProvider<JavaCompile>
    private lateinit var compiledClasses: ConfigurableFileCollection

    override fun apply(project: Project) {
        project.tasks {
            generateR = register("generate$CLASS_NAME", RTask::class) {
                group = GROUP_NAME
            }

            project.afterEvaluate {
                generateR {
                    if (packageName == null) packageName = project.group.toString()
                }
            }

            compileR = register("compile$CLASS_NAME", JavaCompile::class) {
                dependsOn(generateR.get())
                group = GROUP_NAME
                classpath = project.files()
                destinationDir = project.buildDir.resolve("$GENERATED_DIRECTORY/r/classes/main")
                generateR {
                    source(outputDirectory)
                }
            }

            compiledClasses = project.files(compileR.get().outputs.files.filter {
                !it.name.endsWith("dependency-cache")
            })
            compiledClasses.builtBy(compileR.get())
            project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName("main") {
                compileClasspath += compiledClasses
                compiledClasses.forEach { output.dir(it) }
            }
        }

        require(project.plugins.hasPlugin("org.gradle.idea")) { "Plugin 'idea' must be applied." }

        project.configurations.register("provided$CLASS_NAME") {
            dependencies += project.dependencies.create(compiledClasses)
            (project.extensions["idea"] as IdeaModel).module.scopes["PROVIDED"]!!["plus"]!! += this
        }
    }

    internal companion object {
        const val CLASS_NAME = "R"
        const val GROUP_NAME = "generation"
        const val GENERATED_DIRECTORY = "generated"
    }
}