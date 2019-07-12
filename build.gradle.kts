plugins {
    id("flavor.pie.promptsign") version "1.1.0"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    `java-library`
}

group = "flavor.pie"
version = "1.7.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.spongepowered.org/maven/")
    }
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public")
    }
}

configurations {
    implementation {
        extendsFrom(shadow.get())
    }
}

dependencies {
    api("org.spongepowered:spongeapi:7.1.0")
    shadow("org.bstats:bstats-sponge-lite:1.4")
}

tasks.jar {
    enabled = false
}
tasks.build {
    dependsOn(tasks.shadowJar)
}
tasks.signArchives {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier.set("")
    configurations = listOf(project.configurations.shadow.get())
}
