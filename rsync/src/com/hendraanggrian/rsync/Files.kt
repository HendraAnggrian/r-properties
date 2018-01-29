@file:Suppress("NOTHING_TO_INLINE")

package com.hendraanggrian.rsync

import java.io.File
import java.nio.file.Files.walk
import java.nio.file.Path
import java.util.stream.Stream

internal inline fun Path.list(): Stream<Path> = walk(this).skip(1)

internal inline val File.isValid: Boolean
    get() = !isHidden && !name.startsWith(".")

internal inline val File.isResourceBundle: Boolean
    get() = isValid && extension == "properties" && nameWithoutExtension.let { name ->
        name.contains("_") && name.substringAfterLast("_").length == 2
    }

internal inline val File.resourceBundleName: String
    get() = nameWithoutExtension.substringBeforeLast("_")