package com.hendraanggrian.rsync

/** Extension to customize rsync generation, note that all of properties are optional. */
open class RSyncExtension {

    internal var packageName = "rsync"
    internal var className = "R"
    internal var leadingSlash = false
    internal var resDir = "src/main/resources"
    internal var srcDir = "src/main/java"

    /** Package name of generated class, optional. */
    fun packageName(name: String) {
        packageName = name
    }

    /** Name of which class will be generated with, optional. */
    fun className(name: String) {
        className = name
    }

    /** Will add '/' prefix to non-properties resources. */
    fun leadingSlash(enabled: Boolean) {
        leadingSlash = enabled
    }

    /** Path of resources that will be read. */
    fun resDir(dir: String) {
        resDir = dir
    }

    /** Path of which R class is generated to. */
    fun srcDir(dir: String) {
        srcDir = dir
    }
}