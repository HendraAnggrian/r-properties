package com.hendraanggrian.rsync

/** Extension to customize rsync generation, note that all of this is optional. */
open class RSyncExtension {

    internal var mPackageName: String = "rsync"
    internal var mClassName: String = "R"
    internal var mLeadingSlash: Boolean = false
    internal var mDebug: Boolean = false
    internal var mResDir: String = "src/main/resources"
    internal var mSrcDir: String = "src/main/java"
    internal var mIgnore: Array<out String> = emptyArray()

    /** Package name of generated class, optional. */
    fun packageName(name: String) {
        mPackageName = name
    }

    /** Name of which class will be generated with, optional. */
    fun className(name: String) {
        mClassName = name
    }

    /** Will add '/' prefix to non-properties resources. */
    fun leadingSlash(enabled: Boolean) {
        mLeadingSlash = enabled
    }

    /** Will print output of rsync process. */
    fun debug(enabled: Boolean) {
        mDebug = enabled
    }

    /** Path of resources that will be read. */
    fun resDir(dir: String) {
        mResDir = dir
    }

    /** Path of which R class is generated to. */
    fun srcDir(dir: String) {
        mSrcDir = dir
    }

    /** Skips these properties files, optional. */
    fun ignoreFiles(vararg files: String) {
        mIgnore = files
    }
}