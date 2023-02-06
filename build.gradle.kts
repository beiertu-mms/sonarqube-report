import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "1.8.10"

    id("com.github.johnrengelman.shadow") version "7.1.2"

    application
}

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.github.ajalt.clikt:clikt:3.5.1")

    implementation(platform("org.http4k:http4k-bom:4.38.0.1"))
    implementation("org.http4k:http4k-client-apache")
    implementation("org.http4k:http4k-format-jackson")

    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("org.slf4j:slf4j-simple:2.0.6")

    // testing
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testImplementation("io.strikt:strikt-core:0.34.1")
    testImplementation("io.mockk:mockk:1.13.4")
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            showCauses = true
            exceptionFormat = TestExceptionFormat.SHORT
            events = setOf(
                TestLogEvent.PASSED,
                TestLogEvent.FAILED,
                TestLogEvent.SKIPPED
            )
            showExceptions = true
            afterSuite(
                KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
                    if (desc.parent == null) {
                        val output = "Results: ${result.resultType} (${result.testCount} tests, " +
                            "${result.successfulTestCount} passed, " +
                            "${result.failedTestCount} failed, " +
                            "${result.skippedTestCount} skipped)"
                        val startItem = "| "
                        val endItem = " |"
                        val repeatLength = startItem.length + output.length + endItem.length
                        println("\n" + ("-".repeat(repeatLength)) + "\n" + startItem + output + endItem + "\n" + ("-".repeat(repeatLength)))
                    }
                })
            )
        }
    }

    register<Copy>("packageDistribution") {
        dependsOn("shadowJar")
        from("${project.rootDir}/scripts/sonarqube-report.sh")
        from("${project.buildDir}/libs/${project.name}.jar")
        into("${project.buildDir}/dist")
    }
}

application {
    // Define the main class for the application.
    mainClass.set("com.github.beiertumms.sonarqubereport.AppKt")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("sonarqube-report")
    archiveClassifier.set("")
    archiveVersion.set("")
}
