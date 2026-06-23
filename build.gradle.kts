plugins {
    kotlin("jvm") version "1.9.22"
    id("maven-publish")
}

group = "com.github.jwagner"
version = "2.0.5"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group.toString()
            artifactId = "smartcrop"
            version = version
        }
    }
}
