plugins {
    java
    jacoco
}

dependencies {
    implementation(project(":cinematic-core"))
    implementation(project(":cinematic-runtime"))
    implementation(project(":cinematic-camera"))
    implementation(project(":cinematic-actors"))
    implementation(project(":cinematic-adapters"))
    implementation(project(":cinematic-dsl"))
    compileOnly(libs.paper.api)
    compileOnly(libs.protocollib)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.paper.api)
    testImplementation("org.mockito:mockito-core:5.15.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.1")
}

tasks.jar {
    dependsOn(configurations.runtimeClasspath)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    )
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "PACKAGE"
            includes = listOf(
                "com.darkbladedev.cinematic.bootstrap.command.subcommands"
            )
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
