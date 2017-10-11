package com.hendraanggrian.rsync

/**
 * Extension to customize rsync generation, note that all of this is optional.
 *
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
open class RSyncExtension {

    /** Package name of generated class, optional. */
    var packageName: String = "rsync"

    /** Name of which class will be generated with, optional. */
    var className: String = "R"

    /** Will add '/' prefix to non-properties resources. */
    var leadingSlash: Boolean = false

    /** Path of resources that will be read. */
    var resDir: String = "src/main/resources"

    /** Path of which R class is generated to. */
    var srcDir: String = "src/main/java"

    /** Skips these properties files, optional. */
    var ignoreFile: String? = null
    var ignoreFiles: Array<String>? = null

    /** Will print output of rsync process. */
    var debug: Boolean = false

    internal val ignoreList: List<String>
        get() = mutableListOf<String>().apply {
            ignoreFile?.let { add(it) }
            ignoreFiles?.let { addAll(it) }
        }
}