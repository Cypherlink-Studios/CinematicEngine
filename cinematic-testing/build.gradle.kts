plugins {
    `java-library`
}

dependencies {
    testImplementation(project(":cinematic-core"))
    testImplementation(project(":cinematic-runtime"))
    testImplementation(project(":cinematic-camera"))
    testImplementation(project(":cinematic-adapters"))
    testImplementation(libs.joml)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.1")
}
