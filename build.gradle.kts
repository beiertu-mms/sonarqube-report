import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.7.10"

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

    implementation("com.github.ajalt.clikt:clikt:3.5.0")
}

tasks {
    test {
        useJUnitPlatform()
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
