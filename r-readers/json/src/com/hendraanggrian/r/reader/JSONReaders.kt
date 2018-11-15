package com.hendraanggrian.r.reader

import com.hendraanggrian.r.addFieldIfNotExist
import com.hendraanggrian.r.newField
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.lang.ref.WeakReference

@Suppress("Unused")
class JSONReader : Reader<String>({ task, typeBuilder, file ->
    if (file.extension == "json") {
        file.reader().use { reader ->
            var parser = parserRef.get()
            if (parser == null) {
                parser = JSONParser()
                parserRef = WeakReference(parser!!)
            }
            (parser!!.parse(reader) as JSONObject).forEachKey { key ->
                typeBuilder.addFieldIfNotExist(newField(task.name(key), key))
            }
            "json"
        }
    }
    null
})

private var parserRef = WeakReference<JSONParser>(null)

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