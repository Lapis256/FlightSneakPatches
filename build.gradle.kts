import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("java")
    id("java-library")
    id("idea")

    alias(libs.plugins.forge)
    alias(libs.plugins.parchmentmc)
}

val modId = Constants.Mod.id
val minecraftVersion: String = libs.versions.minecraft.get()
val forgeMajorVersion: String = extractVersionSegments(libs.versions.forge)
val jdkVersion = 17


base {
    archivesName = project.name
    version = Constants.Mod.version
    group = Constants.Mod.group
}

minecraft {
    mappings("parchment", "${libs.versions.parchmentmc.get()}-$minecraftVersion")

    copyIdeResources = true

    runs {
        configureEach {
            ideaModule("${rootProject.name}.${project.name}.main")

            properties(
                mapOf(
                    "forge.logging.markers" to "REGISTRIES", "forge.logging.console.level" to "debug"
                )
            )

            mods {
                create(modId) {
                    source(sourceSets["main"])
                }
            }
        }

        create("client") {
            taskName("Forge Client")

            workingDirectory(project.file("run"))
        }

        create("server") {
            taskName("Forge Server")

            workingDirectory(project.file("run-server"))
        }
    }
}

repositories {
    mavenCentral()
    maven {
        name = "JEI"
        url = uri("https://modmaven.dev/")
    }
    maven {
        name = "Curse Maven"
        url = uri("https://cursemaven.com")
    }
}

dependencies {
    minecraft(libs.minecraftForge)

    libs.bundles.runtimeOnly.get().forEach { runtimeOnly(deobf(it)) }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = jdkVersion
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(jdkVersion)
            vendor = JvmVendorSpec.JETBRAINS
        }
        JavaVersion.toVersion(jdkVersion).let {
            sourceCompatibility = it
            targetCompatibility = it
        }
    }

    processResources {
        val prop: Map<String, String> = mapOf(
            "version" to Constants.Mod.version,
            "group" to Constants.Mod.group,
            "minecraft_version" to minecraftVersion,
            "mod_loader" to "lowcodefml",
            "mod_loader_version_range" to "[38,54)",
            "minecraft_version_range" to "[1.18,1.21.4)",
            "mod_name" to Constants.Mod.name,
            "mod_author" to Constants.Mod.author,
            "mod_id" to Constants.Mod.id,
            "logo_file" to "logo.png",
            "license" to Constants.Mod.license,
            "description" to Constants.Mod.description,
            "display_url" to Constants.Mod.repositoryUrl,
            "display_test" to DisplayTest.IGNORE_SERVER_VERSION.toString(),
            "issue_tracker_url" to Constants.Mod.issueTrackerUrl,
        )

        filesMatching(listOf("pack.mcmeta", "META-INF/mods.toml")) {
            expand(prop)
        }
        inputs.properties(prop)
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "LICENSE_${Constants.Mod.id}" }
        }

        manifest {
            attributes(
                "Specification-Title" to Constants.Mod.name,
                "Specification-Vendor" to Constants.Mod.author,
                "Specification-Version" to version,
                "Implementation-Title" to project.name,
                "Implementation-Version" to version,
                "Implementation-Vendor" to Constants.Mod.author,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                "Timestamp" to System.currentTimeMillis(),
                "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})",
                "Built-On-Minecraft" to minecraftVersion,
            )
        }

        finalizedBy("reobfJar")
    }
}

fun deobf(dependency: Provider<MinimalExternalModuleDependency>) = deobf(dependency.get())
fun deobf(dependency: MinimalExternalModuleDependency) = fg.deobf(dependency)
