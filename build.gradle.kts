plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    checkstyle
}

group = "com.loki"
version = "3.0.0"
description = "Multi-region mine management plugin for Paper"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("commons-io:commons-io:2.18.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.10")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-property:5.9.1")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation("net.kyori:adventure-api:4.17.0")
    testImplementation("net.kyori:adventure-text-minimessage:4.17.0")
}

checkstyle {
    toolVersion = "10.21.4"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxErrors = 0
    maxWarnings = 0
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks {

    shadowJar {
        archiveClassifier.set("")

        // Temporary: disable relocate to avoid ASM Java 21 bytecode issues
        // relocate(
        //     "dev.loki.lomines.libs.lolib",
        //     "com.loki.lomines.libs.lolib"
        // )
    }

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        val props = mapOf(
            "version" to version,
            "description" to description
        )

        inputs.properties(props)
        filteringCharset = "UTF-8"

        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}