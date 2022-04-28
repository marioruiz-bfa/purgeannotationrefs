plugins {
    `java-library`
}

group = "org.dyndns.fichtner.purgeannotationrefs"
version = "0.38-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.3")
    implementation("org.ow2.asm:asm-util:9.3")
    implementation("org.ow2.asm:asm-tree:9.3")
    implementation("org.apache.ant:ant:1.10.12")
    implementation("org.apache.maven:maven-plugin-api:3.8.5")
    implementation("org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.4")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}