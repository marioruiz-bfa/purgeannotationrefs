plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.0.0-rc-1"
}

repositories {
    mavenCentral()
}

dependencies {
    gradleApi()
    implementation(project(":library"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("annotationPurgePlugin") {
            implementationClass = "org.dyndns.fichtner.purgeannotationrefs.gradle.PurgeAnnotationRefsPlugin"
            id = "org.dyndns.fichtner.purgeannotationrefs"
            displayName = "Purge Annotation Refs Gradle Plugin"
            description = "Remove references to annotations out of the java bytecode/classfiles (remove the @Anno tag from annotated elements)"
        }
    }
}
pluginBundle {
    website = "https://github.com/marioruiz-bfa/purgeannotationrefs"
    vcsUrl = "https://github.com/marioruiz-bfa/purgeannotationrefs"
    tags = listOf("annotations", "purgeannotationrefs", "annotationeraser")
}
