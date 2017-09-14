package com.hendraanggrian.rsync

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
open class RSyncExtension {

    /** Package name of generated class, optional. */
    var packageName: String = "r"

    /** Name of which class will be generated with, optional. */
    var className: String = "R"

    /** Automatic detection not yet supported, relies for absolute path for now. */
    var pathToResources: String = "src/main/resources"

    /** Automatic detection not yet supported, relies for absolute path for now. */
    var pathToJava: String = "src/main/java"

    /** Skips these properties files, optional. */
    var ignore: Array<String> = emptyArray()
}