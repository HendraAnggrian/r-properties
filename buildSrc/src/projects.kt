import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.io.File

fun Project.deployable() {
    tasks.register("deploy") {
        dependsOn("build")
        projectDir.resolve("build/libs")?.listFiles()?.forEach {
            it.renameTo(File(rootDir.resolve("r-integration-tests"), it.name))
        }
    }
}

fun Project.ktlint(
    extraDependency: (Configuration.(
        add: (dependencyNotation: Any) -> Unit
    ) -> Unit)? = null
) {
    val configuration = configurations.register("ktlint")

    dependencies {
        configuration {
            invoke("com.github.shyiko:ktlint:$VERSION_KTLINT")
            extraDependency?.invoke(this) { dependencyNotation ->
                invoke(dependencyNotation)
            }
        }
    }
    tasks {
        val ktlint = register("ktlint", JavaExec::class) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            inputs.dir("src")
            outputs.dir("src")
            description = "Check Kotlin code style."
            classpath(configuration.get())
            main = "com.github.shyiko.ktlint.Main"
            args("src/**/*.kt")
        }
        "check" {
            dependsOn(ktlint.get())
        }
        register("ktlintFormat", JavaExec::class) {
            group = "formatting"
            inputs.dir("src")
            outputs.dir("src")
            description = "Fix Kotlin code style deviations."
            classpath(configuration.get())
            main = "com.github.shyiko.ktlint.Main"
            args("-F", "src/**/*.kt")
        }
    }
}