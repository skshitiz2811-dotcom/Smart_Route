import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

val newBuildDir: Directory =
    rootProject.layout.buildDirectory
        .dir("../../build")
        .get()

rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory =
        newBuildDir.dir(project.name)

    project.layout.buildDirectory.value(newSubprojectBuildDir)
}

subprojects {
    project.evaluationDependsOn(":app")
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    val subproject = this

    pluginManager.withPlugin("com.android.library") {
        subproject.extensions.configure(LibraryExtension::class.java) {
            if (namespace == null) {
                namespace = subproject.group.toString()
            }
        }
    }
}

// Third-party plugins compatibility
subprojects {
    pluginManager.withPlugin("com.android.library") {
        extensions.configure<LibraryExtension> {
            compileSdk = 36
        }
    }
}

gradle.projectsEvaluated {
    subprojects.forEach { project ->
        project.tasks.withType<KotlinCompile>().configureEach {
            when (project.name) {
                "telephony",
                "flutter_tts" -> {
                    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
                }

                else -> {
                    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }
}