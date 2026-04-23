plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.loki"
version = "3.0.0"
description = "Multi-region mine management plugin for Paper"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

// LoAPI: libs/lolib*.jar — fileTree индексируется Gradle / Java LS стабильнее, чем files(singleJar)
val lolibTree = fileTree("libs") { include("lolib*.jar") }
if (lolibTree.files.isEmpty()) {
    throw GradleException(
        "Missing libs/lolib*.jar — add LoAPI fat jar to ${rootProject.file("libs").absolutePath}"
    )
}

dependencies {
    // Paper API 1.21
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    
    // LoAPI (local)
    implementation(lolibTree)
    
    // Runtime dependencies (loaded via DependencyManager from LoAPI)
    // These are marked as compileOnly because they will be downloaded at runtime
    compileOnly("org.apache.commons:commons-math3:3.6.1")
    compileOnly("commons-io:commons-io:2.17.0")
    
    // Optional plugin integrations (soft dependencies)
    compileOnly("com.github.oraxen:oraxen:1.171.0")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")
    
    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
    testImplementation("io.kotest:kotest-assertions-core:5.7.2")
    testImplementation("io.kotest:kotest-property:5.7.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    testImplementation("org.apache.commons:commons-math3:3.6.1")
    testImplementation("commons-io:commons-io:2.17.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        
        // Relocate LoAPI to avoid conflicts
        relocate("dev.lolib", "com.loki.lomines.libs.lolib")
        
        // Don't minimize - causes issues with Java 21
        // minimize()
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
