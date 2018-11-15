package com.hendraanggrian.generating.r.reader

import com.hendraanggrian.generating.r.RTask
import com.hendraanggrian.generating.r.newField
import com.squareup.javapoet.TypeSpec
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.lang.ref.WeakReference

@Suppress("Unused")
internal object JSONReader : Reader<String> {

    var parserRef = WeakReference<JSONParser>(null)

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File): String? {
        if (file.extension == "json") {
            file.reader().use { reader ->
                var parser = parserRef.get()
                if (parser == null) {
                    parser = JSONParser()
                    parserRef = WeakReference(parser)
                }
                (parser.parse(reader) as JSONObject).forEachKey(task) { key ->
                    typeBuilder.addFieldIfNotExist(newField(task.name(key), key))
                }
                return "json"
            }
        }
        return null
    }

    private fun JSONObject.forEachKey(task: RTask, action: (String) -> Unit) {
        forEach { key, value ->
            action(key.toString())
            if (value is JSONArray && task.json.isRecursive) {
                value.forEachKey(task, action)
            }
        }
    }

    private fun JSONArray.forEachKey(task: RTask, action: (String) -> Unit) {
        forEach { json ->
            when {
                json is JSONObject -> json.forEachKey(task, action)
                json is JSONArray && task.json.isRecursive -> json.forEachKey(task, action)
            }
        }
    }
}