import net.minecrell.pluginyml.paper.PaperPluginDescription
plugins {
	id("java")
	id("de.eldoria.plugin-yml.paper") version "0.7.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("com.gradleup.shadow") version "9.0.0-rc1"
}

// TODO: Change the information in here to the information you need.
// The name is located in settings.gradle.kts.
group = "com.coqing.coqingmusic"
version = "1.0.0"
description = "A plugin template for Coqing's plugins."

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://maven.faved.top/releases")
}

// When specifying dependencies, make sure to follow these rules:
// - If you want to shade the dependency you want to add, use implementation().
// - If you want to load the dependency during runtime, use paperLibrary().
// - If you want to include a plugin API, use compileOnly().
dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    implementation("com.github.stefvanschie.inventoryframework:IF:0.11.2")
    compileOnly("com.coqing:coqingutils:1.0.0")
    paperLibrary("net.raphimc:NoteBlockLib:3.1.0")

}

tasks.build {
    dependsOn("shadowJar")
}

tasks.shadowJar {
    // relocate("com.github.stefvanschie.inventoryframework", "com.coqing.coqingmusic.inventoryframework")
}

// Configuring paper-plugin.yml
paper {
    authors = listOf("Coqing")
    main = "com.coqing.coqingmusic.CoqingMusic"
    loader = "com.coqing.coqingmusic.load.LibraryLoader"
    apiVersion = "1.21.4"

    // Keep this on!
    generateLibrariesJson = true
    
    serverDependencies {
        // This plugin is REQUIRED!!!!!
        register("CoqingUtils") {
            required = true
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        // Specify other dependencies here...
    }
}

// Set to Mojang mappings
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}
