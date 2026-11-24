plugins {
    id("dev.architectury.loom") version "1.9-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
}

subprojects {
    apply(plugin = "java")

    group = property("maven_group") as String
    version = property("mod_version") as String

    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.impactdev.net/repository/development/")
        maven("https://maven.parchmentmc.org")
        mavenCentral()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release = 21
        options.encoding = "UTF-8"
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
        withSourcesJar()
    }

    tasks.withType<ProcessResources>().configureEach {
        val replaceProperties = mapOf("version" to project.property("mod_version") as String)
        inputs.properties(replaceProperties)
        filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "fabric.mod.json")) {
            expand(replaceProperties)
        }
    }
}

val modrinthProfile = file("D:/03_Games/Modrinth/profiles/Cobblemon Official Modpack [Fabric]/mods")

val deployFabricJar by tasks.registering(Copy::class) {
    from(layout.projectDirectory.dir("fabric/build/libs")) {
        include("*.jar")
        exclude("*-dev.jar", "*-sources.jar", "*-slim.jar")
    }
    into(modrinthProfile)
    onlyIf { modrinthProfile.exists() }
}

gradle.projectsEvaluated {
    project(":fabric").tasks.named("build") {
        finalizedBy(deployFabricJar)
    }
}
