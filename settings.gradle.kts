include(RELEASE_ARTIFACT)

/*
File("r-integration-tests")
    .walk()
    .filter { it.isDirectory && it.name == "simple" }
    .forEach { include("r-integration-tests:${it.name}") }*/
