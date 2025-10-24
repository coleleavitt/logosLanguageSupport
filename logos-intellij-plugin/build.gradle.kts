plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "com.coleleavitt.logos"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://www.jetbrains.com/intellij-repository/releases")
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")

    intellijPlatform {
        defaultRepositories()
        marketplace()
    }
}

dependencies {
    intellijPlatform {
        rustRover("2024.3")

        instrumentationTools()
    }

    // Include our LSP server components
    implementation(project(":app"))
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        name = "Logos Language Support"
        version = project.version.toString()
        description = """
            Comprehensive language support for Logos (.x, .xm, .xi, .xmi files) used in Theos tweak development.

            Features:
            - Syntax highlighting for Logos directives
            - Code completion for directives and Objective-C classes
            - Hover documentation
            - Real-time diagnostics
            - Symbol navigation
            - File templates
        """.trimIndent()

        changeNotes = """
            <ul>
                <li>1.0.0 - Initial release with full LSP support</li>
            </ul>
        """.trimIndent()

        ideaVersion {
            sinceBuild = "243"
            untilBuild = "253.*"  // Support up to 2025.3
        }
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    verifyPlugin {
        ides {
            recommended()
        }
    }
}

tasks {
    buildSearchableOptions {
        enabled = false
    }

    prepareJarSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        sinceBuild.set("243")
        untilBuild.set("253.*")
    }

    // Build the language server before building the plugin
    prepareSandbox {
        dependsOn(":app:installDist")

        // Copy the language server distribution into the plugin directory
        // Note: files added via from() go into pluginName directory automatically
        val appProject = project(":app")
        val installDirProvider = appProject.layout.buildDirectory.dir("install/app")

        from(installDirProvider) {
            into(pluginName.map { "$it/languageServer" })
        }
    }
}
