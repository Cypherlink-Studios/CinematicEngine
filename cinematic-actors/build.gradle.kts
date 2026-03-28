plugins {
    `java-library`
}

dependencies {
    api(project(":cinematic-core"))
    compileOnly(libs.paper.api)
    testImplementation(libs.junit.jupiter)
}
