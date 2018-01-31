package com.hendraanggrian.r

/** Extension to customize r generation, note that all of properties are optional. */
open class RExtension {

    internal var packageName = "r"
    internal var resDir = "src/main/resources"
    internal var srcDir = "src/main/java"
    internal var leadingSlash = false

    /** Package name of generated class, optional. */
    fun packageName(name: String) {
        packageName = name
    }

    /** Path of resources that will be read. */
    fun resDir(dir: String) {
        resDir = dir
    }

    /** Path of which R class is generated to. */
    fun srcDir(dir: String) {
        srcDir = dir
    }

    /** Will add '/' prefix to non-properties resources. */
    fun leadingSlash(enabled: Boolean) {
        leadingSlash = enabled
    }
}