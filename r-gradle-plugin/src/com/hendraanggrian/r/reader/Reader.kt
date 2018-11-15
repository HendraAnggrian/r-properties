package com.hendraanggrian.r.reader

import com.hendraanggrian.r.RTask
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.io.Serializable

open class Reader<T>(val read: (task: RTask, typeBuilder: TypeSpec.Builder, file: File) -> T?) : Serializable