package com.hendraanggrian.r.adapters

import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.r.JsonSettings
import java.io.File
import java.lang.ref.WeakReference
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

/**
 * An adapter that writes [JSONObject] and [JSONArray] keys.
 * The file path itself will be written with underscore prefix.
 */
internal class JsonAdapter(
    isUppercaseField: Boolean,
    private val settings: JsonSettings
) : BaseAdapter(isUppercaseField) {
    private var parserRef = WeakReference<JSONParser>(null)

    override fun process(typeBuilder: TypeSpecBuilder, file: File): Boolean {
        if (file.extension == "json") {
            file.reader().use { reader ->
                var parser = parserRef.get()
                if (parser == null) {
                    parser = JSONParser()
                    parserRef = WeakReference(parser)
                }
                (parser.parse(reader) as JSONObject).forEachKey { typeBuilder.addField(it) }
                return true
            }
        }
        return false
    }

    private fun JSONObject.forEachKey(action: (String) -> Unit): Unit = forEach { key, value ->
        action(key.toString())
        if (value is JSONArray && settings.isWriteArray) {
            value.forEachKey(action)
        }
    }

    private fun JSONArray.forEachKey(action: (String) -> Unit): Unit = forEach { json ->
        when {
            settings.isRecursive && json is JSONObject -> json.forEachKey(action)
            settings.isWriteArray && json is JSONArray -> json.forEachKey(action)
        }
    }
}
