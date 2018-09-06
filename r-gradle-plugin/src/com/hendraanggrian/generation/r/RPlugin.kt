package com.hendraanggrian.generation.r

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like R class with this plugin. */
class RPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.run {
            tasks {
                val generateR = register("generate$CLASS_NAME", RTask::class) {
                    group = GROUP_NAME
                }
                afterEvaluate {
                    if (generateR.get().packageName == null) generateR.get().packageName = group.toString()
                }

                val compileR = register("compile$CLASS_NAME", JavaCompile::class) {
                    dependsOn(generateR.get())
                    group = GROUP_NAME
                    classpath = files()
                    destinationDir = buildDir.resolve("$GENERATED_DIRECTORY/r/classes/main")
                    source(generateR.get().outputDir)
                }

                val compiledClasses = files(compileR.get().outputs.files.filter { !it.name.endsWith("dependency-cache") })
                compiledClasses.builtBy(compileR.get())

                val sourceSet = convention.getPlugin(JavaPluginConvention::class.java).sourceSets["main"]
                sourceSet.compileClasspath += compiledClasses
                compiledClasses.forEach { sourceSet.output.dir(it) }

                require(plugins.hasPlugin("org.gradle.idea")) { "Plugin 'idea' must be applied." }
                val providedConfig = configurations.register("provided$CLASS_NAME")
                providedConfig.get().dependencies += dependencies.create(compiledClasses)
                (extensions["idea"] as IdeaModel).module.scopes["PROVIDED"]!!["plus"]!! += providedConfig.get()
            }
        }
    }

    internal companion object {
        const val CLASS_NAME = "R"
        const val GROUP_NAME = "generation"
        const val GENERATED_DIRECTORY = "generated"
    }
}