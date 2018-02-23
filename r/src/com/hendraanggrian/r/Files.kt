@file:Suppress("NOTHING_TO_INLINE")

package com.hendraanggrian.r

import java.io.File
import java.lang.Character.isLetter

internal inline val File.isValid: Boolean
    get() = !isHidden && isLetter(name[0])

internal inline val File.isProperties: Boolean
    get() = extension == "properties"

internal inline val File.isResourceBundle: Boolean
    get() = isValid && isProperties && nameWithoutExtension.let { name ->
        name.contains("_") && name.substringAfterLast("_").length == 2
    }

internal inline val File.resourceBundleName: String
    get() = nameWithoutExtension.substringBeforeLast("_")