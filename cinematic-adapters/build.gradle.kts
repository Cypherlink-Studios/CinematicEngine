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
    compileOnly(libs.packetevents.spigot)
    testImplementation(libs.paper.api)
    testImplementation(libs.packetevents.spigot)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation("org.mockito:mockito-core:5.15.2")
    testImplementation("io.netty:netty-buffer:4.1.108.Final")
    testImplementation("com.google.code.gson:gson:2.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.1")
}
