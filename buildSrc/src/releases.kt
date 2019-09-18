const val RELEASE_USER = "hendraanggrian"
const val RELEASE_GROUP = "com.$RELEASE_USER"
const val RELEASE_ARTIFACT = "r-gradle-plugin"
const val RELEASE_VERSION = "0.2"
const val RELEASE_DESC = "Type-safe resources gradle plugin"
const val RELEASE_WEBSITE = "https://github.com/$RELEASE_USER/$RELEASE_ARTIFACT"

val BINTRAY_USER: String = System.getenv("BINTRAY_USER")
val BINTRAY_KEY: String = System.getenv("BINTRAY_KEY")