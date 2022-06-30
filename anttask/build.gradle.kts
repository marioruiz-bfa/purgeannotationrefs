plugins {
  `java-library`
  `maven-publish`
  `java-library-distribution`
}

group = "org.dyndns.fichtner.purgeannotationrefs"
version = "0.38-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":library"))
  implementation("org.apache.ant:ant:1.10.12")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
  useJUnitPlatform()
}

publishing {
  publications {
    register<MavenPublication>("maven") {
      from(components.getByName("java"))
      version = project.version.toString()
      group = project.group
      artifactId = project.name
    }
  }
}