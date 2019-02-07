include(RELEASE_ARTIFACT)
include("website")
// includeDir("integration-tests")

fun includeDir(dir: String) = file(dir)
    .listFiles()
    .filter { it.isDirectory }
    .forEach {
        if (it.name != "configuration-custom")
            include("$dir:${it.name}")
    }