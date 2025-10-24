plugins {
    id("java")
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.intellij.platform") version "2.1.0"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
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

    // Configure the lexer generation task
    withType<org.jetbrains.grammarkit.tasks.GenerateLexerTask> {
        sourceFile.set(file("src/main/grammer/Logos.flex"))
        targetOutputDir.set(file("src/main/gen/com/coleleavitt/logos/intellij"))
        purgeOldFiles.set(true)
    }

    // Make compileKotlin depend on lexer generation
    compileKotlin {
        dependsOn("generateLexer")
    }
}

// Add generated sources to source sets
sourceSets {
    main {
        java.srcDirs("src/main/gen")
    }
}
