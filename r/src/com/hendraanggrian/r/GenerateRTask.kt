package com.hendraanggrian.r

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException

open class GenerateRTask : DefaultTask() {

    @Input lateinit var writer: RClassWriter
    @OutputDirectory lateinit var outputDirectory: File

    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        outputDirectory.deleteRecursively()
        writer.write(outputDirectory)
    }
}