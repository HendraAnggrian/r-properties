package com.hendraanggrian.r

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter
import kotlin.DeprecationLevel.ERROR

/** Extension to customize r generation, note that all customizations are optional. */
open class RExtension {

    private var _packageName: String = "r"
    private var _resDir = "src/main/resources"
    private var _srcDir = "src/main/java"
    private var _leadingSlash = true

    /** Package name of generated class. */
    var packageName: String
        @Deprecated(NO_GETTER, level = ERROR) get() = throw UnsupportedOperationException(NO_GETTER)
        set(value) {
            // TODO: check if input is valid java package
            require(value.isNotBlank()) { "Package name must not be blank!" }
            _packageName = value
        }

    /** Path of resources that will be read. */
    var resDir: String
        @Deprecated(NO_GETTER, level = ERROR) get() = throw UnsupportedOperationException(NO_GETTER)
        set(value) {
            _resDir = value
        }

    /** Path of which R class is generated to. */
    var srcDir: String
        @Deprecated(NO_GETTER, level = ERROR) get() = throw UnsupportedOperationException(NO_GETTER)
        set(value) {
            _srcDir = value
        }

    /** Will add '/' prefix to non-properties resources. */
    var leadingSlash: Boolean
        @Deprecated(NO_GETTER, level = ERROR) get() = throw UnsupportedOperationException(NO_GETTER)
        set(value) {
            _leadingSlash = value
        }

    internal val packagePaths: Array<String> get() = _packageName.split('.').toTypedArray()
    internal val resFile: File get() = File(_resDir)
    internal val srcFile: File get() = File(_srcDir)

    internal fun getPath(path: String): String = when {
        _leadingSlash -> "/$path"
        else -> path
    }

    internal fun toJavaFile(typeSpec: TypeSpec): JavaFile = JavaFile.builder(_packageName, typeSpec)
            .addFileComment("r generated this class at ${now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            .build()

    companion object {
        private const val NO_GETTER: String = "Property does not have a getter"
    }
}