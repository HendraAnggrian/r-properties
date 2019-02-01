package com.hendraanggrian.generating.r.converters

import com.hendraanggrian.generating.r.addStringField
import com.hendraanggrian.generating.r.configuration.JsonConfiguration
import com.squareup.javapoet.TypeSpec
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.lang.ref.WeakReference

internal class JsonConverter(private val configuration: JsonConfiguration) : Converter {

    var parserRef = WeakReference<JSONParser>(null)

    override fun convert(typeBuilder: TypeSpec.Builder, file: File): Boolean {
        if (file.extension == "json") {
            file.reader().use { reader ->
                var parser = parserRef.get()
                if (parser == null) {
                    parser = JSONParser()
                    parserRef = WeakReference(parser)
                }
                (parser.parse(reader) as JSONObject).forEachKey { key ->
                    typeBuilder.addStringField(key, key)
                }
                return true
            }
        }
        return false
    }

    private fun JSONObject.forEachKey(action: (String) -> Unit) {
        forEach { key, value ->
            action(key.toString())
            if (value is JSONArray && configuration.readArray) {
                value.forEachKey(action)
            }
        }
    }

    private fun JSONArray.forEachKey(action: (String) -> Unit) {
        forEach { json ->
            when {
                configuration.isRecursive && json is JSONObject -> json.forEachKey(action)
                configuration.readArray && json is JSONArray -> json.forEachKey(action)
            }
        }
    }
}