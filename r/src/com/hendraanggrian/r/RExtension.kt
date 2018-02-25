package com.hendraanggrian.r

import kotlin.DeprecationLevel.ERROR

/** Extension to customize r generation, note that all customizations are optional. */
open class RExtension {

    internal var _packageName: String? = null
    internal var _resourcesDir: String? = null

    /** Package name of which `R` will be generated to, default is project group. */
    var packageName: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) {
            _packageName = value
        }

    /** Path of resources that will be read. */
    var resourcesDir: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) {
            _resourcesDir = value
        }

    companion object {
        private const val NO_GETTER: String = "Property does not have a getter"

        private fun noGetter(): Nothing = throw UnsupportedOperationException(NO_GETTER)
    }
}