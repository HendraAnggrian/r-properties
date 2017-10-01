@file:JvmName("UtilsKt")
@file:Suppress("NOTHING_TO_INLINE")

package com.hendraanggrian.rsync

internal inline fun Iterable<String>.filterInternationalizedProperties(): List<String> = filter { it.contains("_") && it.substringAfter('_').length == 2 }

internal inline fun Iterable<String>.distinctInternationalizedPropertiesIdentifier(): List<String> = map { it.substringBefore("_") }.distinct()