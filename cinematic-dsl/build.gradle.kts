plugins {
    `java-library`
}

dependencies {
    api(project(":cinematic-core"))
    api(project(":cinematic-camera"))
    api(project(":cinematic-actors"))
    api(libs.joml)
    implementation(libs.snakeyaml)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.1")
}
