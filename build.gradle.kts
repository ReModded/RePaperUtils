import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.5"
    id("xyz.jpenilla.run-paper") version "2.0.1" // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3" // Generates plugin.yml
}

group = "net.remodded"
version = "1.0.0"
description = ""

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()

    maven("https://repo.codemc.org/repository/maven-public/") //PVPManager
    maven("https://jitpack.io") //PVPManager
}

dependencies {
    paperDevBundle("1.19.4-R0.1-SNAPSHOT")
    implementation("me.NoChance.PvPManager:PvPManager:3.10.0")
    compileOnly("com.github.angeschossen:LandsAPI:6.8.3")

    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    /*
    reobfJar {
      // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
      // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
      outputJar.set(layout.buildDirectory.file("libs/PaperweightTestPlugin-${project.version}.jar"))
    }
     */
}

// Configure plugin.yml generation
bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "net.remodded.repaperutils.RePaperUtils"
    apiVersion = "1.19"
    authors = listOf("Twarug", "DEv0on")
    softDepend = listOf(
        "Lands", "PvPManager"
    )
    loadBefore = listOf(
        "Lands", "PvPManager"
    )
}
