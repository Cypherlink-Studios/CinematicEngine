plugins {
    `java-library`
}

dependencies {
    api(project(":cinematic-core"))
    compileOnly(libs.paper.api)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.1")
}
