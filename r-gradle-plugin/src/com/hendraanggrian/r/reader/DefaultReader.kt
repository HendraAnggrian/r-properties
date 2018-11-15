package com.hendraanggrian.r.reader

import com.hendraanggrian.r.addFieldIfNotExist
import com.hendraanggrian.r.newField

internal open class DefaultReader(prefix: String?) : Reader<Unit>({ task, typeBuilder, file ->
    typeBuilder.addFieldIfNotExist(
        newField(
            task.name(prefix?.let { "${it}_" }.orEmpty() + file.nameWithoutExtension),
            file.path.substringAfter(task.resourcesDir.path)
        )
    )
}) {

    companion object : DefaultReader(null)
}