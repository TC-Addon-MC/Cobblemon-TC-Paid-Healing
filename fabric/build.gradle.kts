plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

val minecraft_version: String by project
val loader_version: String by project
val fabric_version: String by project
val parchment_version: String by project
val parchment_mc_version: String by project
val cobblemon_version: String by project
val mod_version: String by project

version = mod_version
base { archivesName = "cobblemon-tc-paid-healing-${minecraft_version}-fabric" }

architectury {
    platformSetupLoomIde()
    fabric()
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
    modImplementation("net.fabricmc:fabric-loader:${loader_version}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
    modImplementation("com.cobblemon:fabric:${cobblemon_version}")

    implementation(project(path = ":common", configuration = "namedElements"))
    implementation(project(path = ":client", configuration = "namedElements"))
}

tasks.named<org.gradle.jvm.tasks.Jar>("jar") {
    from(project(":common").sourceSets["main"].output)
    from(project(":client").sourceSets["main"].output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
