plugins {
    `java-library`
}

dependencies {
    api(project(":cinematic-core"))
    api(project(":cinematic-runtime"))
    api(project(":cinematic-camera"))
    api(project(":cinematic-actors"))
    compileOnly(libs.paper.api)
    compileOnly(libs.protocollib)
    testImplementation(libs.junit.jupiter)
}
