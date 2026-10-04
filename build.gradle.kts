plugins {
    base
}

subprojects {
    apply(plugin = "java-library")

    group = "com.darkbladedev"
    version = "1.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-releases/")
    }

    extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 21
    }

    tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
        useJUnitPlatform()
    }
}
