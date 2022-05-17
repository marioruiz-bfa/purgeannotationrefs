
plugins {
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

subprojects {
    this.afterEvaluate {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
}

group = "org.dyndns.fichtner.purgeannotationrefs"
version = "0.38-SNAPSHOT"

repositories {
    mavenCentral()
}