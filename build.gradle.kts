import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.4"
    id("xyz.jpenilla.run-paper") version "2.3.1" // Adds runServer and runMojangMappedServer tasks for testing
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.2.0"
}

group = "net.remodded"
version = "1.0.0"
description = ""

repositories {
    mavenCentral()

    maven("https://repo.codemc.org/repository/maven-public/") //PVPManager
    maven("https://jitpack.io") //PVPManager
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    implementation("me.NoChance.PvPManager:pvpmanager:3.18.16")
    compileOnly("com.github.angeschossen:LandsAPI:7.9.17")

    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}

paperPluginYaml {
    name = project.name
    version = project.version.toString()
    description = project.description
    main = "net.remodded.repaperutils.RePaperUtils"

    apiVersion = "1.21.1"
    dependencies {
        server("Lands", PaperPluginYaml.Load.BEFORE, false)
        server("PvPManager", PaperPluginYaml.Load.BEFORE, false)
    }
}
