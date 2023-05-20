plugins {
    id("java-library")
}

group = "io.github.dualuse"
version = "3.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://dualuse.github.io/maven/")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    testImplementation("de.dualuse:Swinger:1.+");
    testImplementation("de.dualuse:Collective:1.+")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.wrapper {
    this.gradleVersion = "8.1.1"
}