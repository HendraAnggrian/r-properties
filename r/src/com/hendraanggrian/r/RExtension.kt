package com.hendraanggrian.r

import org.gradle.api.Project
import java.io.File
import kotlin.DeprecationLevel.ERROR

/** Extension to customize r generation, note that all customizations are optional. */
open class RExtension(private val project: Project) {

    private var _resourcesDirectory: File = project.projectDir.resolve("src/main/resources")
    private var _packageName: String = project.group.toString()
    private var _className: String = "R"

    /**
     * Path of resources that will be read.
     * Default is resources folder in main module.
     */
    var resourcesDirectory: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) {
            _resourcesDirectory = project.projectDir.resolve(value)
        }

    /**
     * Package name of which `r` class will be generated to.
     * Default is project group.
     */
    var packageName: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) {
            _packageName = value
        }

    /**
     * `r` class name.
     * Default is `R`.
     */
    var className: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) {
            _className = value
        }

    internal fun toConfig() = ClassConfig(_resourcesDirectory.listFiles(), _packageName, _className)

    internal fun getTaskName(prefix: String) = "$prefix$_className"

    companion object {
        private const val NO_GETTER: String = "Property does not have a getter"

        private fun noGetter(): Nothing = throw UnsupportedOperationException(NO_GETTER)
    }
}