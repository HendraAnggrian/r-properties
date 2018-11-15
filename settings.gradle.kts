include(RELEASE_ARTIFACT)
includeDir("r-integration-tests")

fun includeDir(dir: String) = File(dir).walk().filter { it.isDirectory }.forEach { include("$dir:${it.name}") }