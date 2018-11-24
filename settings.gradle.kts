include(RELEASE_ARTIFACT)
include("website")
includeDir("r-integration-tests")

fun includeDir(dir: String) = file(dir)
    .listFiles()
    .filter { it.isDirectory }
    .forEach { include("$dir:${it.name}") }