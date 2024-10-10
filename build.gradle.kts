plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("maven-publish")
}

group = "ca.bungo.textbubble"
version = "1.0-SNAPSHOT"

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}


repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release = 21
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set(project.description)
            }
        }
    }
}

nexusPublishing {
    repositories {
        create("nexus") {
            nexusUrl = uri("https://nexus.bungo.ca/repository/bungo-staging/")
            snapshotRepositoryUrl = uri("https://nexus.bungo.ca/repository/bungo-snapshots/")
            username = findProperty("nexusUsername") as String?
            password = findProperty("nexusPassword") as String?
        }
    }
}