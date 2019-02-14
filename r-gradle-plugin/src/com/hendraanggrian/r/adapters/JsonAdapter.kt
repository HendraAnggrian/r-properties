package com.hendraanggrian.r.adapters

import com.hendraanggrian.r.options.JsonOptions
import com.hendraanggrian.javapoet.TypeSpecBuilder
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.lang.ref.WeakReference

internal class JsonAdapter(options: JsonOptions) : ConfigurableAdapter<JsonOptions>(options) {

    private var parserRef = WeakReference<JSONParser>(null)

    override fun adapt(file: File, builder: TypeSpecBuilder): Boolean {
        if (file.extension == "json") {
            file.reader().use { reader ->
                var parser = parserRef.get()
                if (parser == null) {
                    parser = JSONParser()
                    parserRef = WeakReference(parser)
                }
                (parser.parse(reader) as JSONObject).forEachKey { key ->
                    builder.stringField(key, key)
                }
                return true
            }
        }
        return false
    }

    private fun JSONObject.forEachKey(action: (String) -> Unit) {
        forEach { key, value ->
            action(key.toString())
            if (value is JSONArray && options.readArray) {
                value.forEachKey(action)
            }
        }
    }

    private fun JSONArray.forEachKey(action: (String) -> Unit) {
        forEach { json ->
            when {
                options.isRecursive && json is JSONObject -> json.forEachKey(action)
                options.readArray && json is JSONArray -> json.forEachKey(action)
            }
        }
    }
}