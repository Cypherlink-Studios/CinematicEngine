plugins {
    `java-library`
}

dependencies {
    api(project(":cinematic-core"))
    api(libs.joml)
    testImplementation(libs.junit.jupiter)
}
