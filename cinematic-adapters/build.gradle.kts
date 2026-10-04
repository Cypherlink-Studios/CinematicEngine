plugins {
    `java-library`
}

dependencies {
    api(project(":cinematic-core"))
    api(project(":cinematic-runtime"))
    api(project(":cinematic-camera"))
    api(project(":cinematic-actors"))
    api(project(":cinematic-dsl"))
    compileOnly(libs.paper.api)
    compileOnly(libs.protocollib)
    testCompileOnly(libs.paper.api)
    testCompileOnly(libs.protocollib)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.1")
}
