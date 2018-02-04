package com.hendraanggrian.r

import kotlin.DeprecationLevel.ERROR

/** Extension to customize r generation, note that all customizations are optional. */
open class RExtension {

    internal var pkgName: String? = null
    internal var resDir: String = "src/main/resources"

    /** Package name of which `R` will be generated to, default is project group. */
    var packageName: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) {
            pkgName = value
        }

    /** Path of resources that will be read. */
    var resourcesDir: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) {
            resDir = value
        }

    companion object {
        private const val NO_GETTER: String = "Property does not have a getter"

        private fun noGetter(): Nothing = throw UnsupportedOperationException(NO_GETTER)
    }
}