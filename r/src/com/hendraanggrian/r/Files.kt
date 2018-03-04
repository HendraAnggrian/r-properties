@file:Suppress("NOTHING_TO_INLINE")

package com.hendraanggrian.r

import java.io.File
import java.lang.Character.isLetter

internal inline fun File.isValid(): Boolean = !isHidden && isLetter(name[0])

internal inline fun File.isProperties(): Boolean = extension == "properties"

internal inline fun File.isResourceBundle(): Boolean = isValid() && isProperties() && nameWithoutExtension.let { name ->
    name.contains("_") && name.substringAfterLast("_").length == 2
}

internal inline val File.resourceBundleName: String get() = nameWithoutExtension.substringBeforeLast("_")