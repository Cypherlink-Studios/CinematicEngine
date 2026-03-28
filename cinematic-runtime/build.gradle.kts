plugins {
    `java-library`
}

dependencies {
    api(project(":cinematic-core"))
    testImplementation(libs.junit.jupiter)
}
