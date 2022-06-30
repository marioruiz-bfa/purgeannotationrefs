//forcing the mojo plugin to behave as if it's 2022.

buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath("org.ow2.asm:asm") {
      version {
        strictly("9.3")
      }
    }
    classpath("org.ow2.asm:asm-util") {
      version {
        strictly("9.3")
      }
    }
    classpath("org.ow2.asm:asm-tree") {
      version {
        strictly("9.3")
      }
    }
    classpath("org.apache.maven:maven-core:3.8.5") {
      version {
        strictly("3.8.5")
      }
    }
    classpath("org.apache.maven:maven-plugin-api:3.8.5") {
      version {
        strictly("3.8.5")
      }
    }
    classpath("org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.4") {
      version {
        strictly("3.6.4")
      }
    }
    classpath("org.apache.maven.plugin-testing:maven-plugin-testing-harness:3.3.0") {
      version {
        strictly("3.3.0")
      }
    }
  }
}



plugins {
  `java-library`
  id("de.benediktritter.maven-plugin-development") version "0.4.0"
}

mavenPlugin {
  this.pluginSourceSet.set(sourceSets.main)
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":library"))
  implementation("org.apache.maven:maven-core:3.8.5")
  implementation("org.apache.maven:maven-plugin-api:3.8.5")
  implementation("org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.4")
  testImplementation("org.codehaus.plexus:plexus-utils:3.4.2")
  testImplementation("org.codehaus.plexus:plexus-container-default:2.1.1")
  testImplementation("org.apache.maven.plugin-testing:maven-plugin-testing-harness:3.3.0")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
  testImplementation("org.hamcrest:hamcrest:2.2")

}

tasks.getByName<Test>("test") {
  useJUnitPlatform()
}

configurations.all {
  resolutionStrategy {
    force("org.apache.maven:maven-core:3.8.5")
    force("org.apache.maven:maven-plugin-api:3.8.5")
    force("org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.4")
    force("org.apache.maven.plugin-testing:maven-plugin-testing-harness:3.3.0")
  }
}