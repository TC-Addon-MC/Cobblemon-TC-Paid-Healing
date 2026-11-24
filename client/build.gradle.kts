plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

val minecraft_version: String by project
val parchment_version: String by project
val parchment_mc_version: String by project
val cobblemon_version: String by project
val mod_version: String by project

version = mod_version
base { archivesName = "cobblemon-tc-paid-healing-client" }

architectury {
    common("fabric", "neoforge")
}

loom {
    silentMojangMappingsLicense()
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${parchment_mc_version}:${parchment_version}@zip")
    })
    implementation(project(path = ":common", configuration = "namedElements"))
    modCompileOnly("com.cobblemon:fabric:${cobblemon_version}")
    compileOnly("net.fabricmc:sponge-mixin:0.17.0+mixin.0.8.7")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-proc:none")
}

configurations.create("commonJava") {
    isCanBeResolved = false
    isCanBeConsumed = true
}

tasks.jar {
    archiveClassifier = "dev"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
