package com.hendraanggrian.generating.r

import com.squareup.javapoet.TypeSpec
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File

class JsonRReader : RReader<String> {

    private val parser = JSONParser()

    override fun read(task: RTask, typeBuilder: TypeSpec.Builder, file: File): String? {
        if (file.extension == "json") {
            file.reader().use { reader ->
                (parser.parse(reader) as JSONObject).forEachKey { key ->
                    typeBuilder.addFieldIfNotExist(newField(task.name(key), key))
                }
                return "json"
            }
        }
        return null
    }

    private fun JSONObject.forEachKey(action: (String) -> Unit) {
        forEach { key, value ->
            action(key.toString())
            if (value is JSONArray) {
                value.forEachKey(action)
            }
        }
    }

    private fun JSONArray.forEachKey(action: (String) -> Unit) {
        forEach { json ->
            when (json) {
                is JSONObject -> json.forEachKey(action)
                is JSONArray -> json.forEachKey(action)
            }
        }
    }
}