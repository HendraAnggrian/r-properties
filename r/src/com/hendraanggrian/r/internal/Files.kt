@file:Suppress("NOTHING_TO_INLINE")

package com.hendraanggrian.r.internal

import java.io.File
import java.lang.Character.isLetter
import java.util.Properties

internal inline fun File.isValid(): Boolean = !isHidden && name.isNotEmpty() && isLetter(name.first())

internal inline fun File.isProperties(): Boolean = extension == "properties"

internal inline fun File.isResourceBundle(): Boolean = isValid() && isProperties() && nameWithoutExtension.let { name ->
    name.contains("_") && name.substringAfterLast("_").length == 2
}

internal inline val File.resourceBundleName: String get() = nameWithoutExtension.substringBeforeLast("_")

internal fun File.forEachProperties(action: (key: String, value: String) -> Unit) = inputStream().use { stream ->
    Properties().run {
        load(stream)
        keys.map { it as? String ?: it.toString() }.forEach { key -> action(key, getProperty(key)) }
    }
}