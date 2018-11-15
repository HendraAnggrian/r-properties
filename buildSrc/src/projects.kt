import org.gradle.api.Project
import org.gradle.kotlin.dsl.TaskContainerScope
import java.io.File

fun Project.deployable(task: TaskContainerScope) {
    task.register("deploy") {
        dependsOn("build")
        projectDir.resolve("build/libs")?.listFiles()?.forEach {
            it.renameTo(File(rootDir.resolve("r-integration-tests"), it.name))
        }
    }
}