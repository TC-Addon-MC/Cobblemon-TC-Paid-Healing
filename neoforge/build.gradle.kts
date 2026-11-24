plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

val minecraft_version: String by project
val neoforge_version: String by project
val parchment_version: String by project
val parchment_mc_version: String by project
val cobblemon_version: String by project
val mod_version: String by project

version = mod_version
base { archivesName = "cobblemon-tc-paid-healing-${minecraft_version}-neoforge" }

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    silentMojangMappingsLicense()
    neoForge {
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${parchment_mc_version}:${parchment_version}@zip")
    })
    "neoForge"("net.neoforged:neoforge:${neoforge_version}")
    modImplementation("com.cobblemon:neoforge:${cobblemon_version}")

    implementation(project(path = ":common", configuration = "namedElements"))
    implementation(project(path = ":client", configuration = "namedElements"))
}

tasks.named<org.gradle.jvm.tasks.Jar>("jar") {
    from(project(":common").sourceSets["main"].output)
    from(project(":client").sourceSets["main"].output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
