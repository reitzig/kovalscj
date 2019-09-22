plugins {
    kotlin("multiplatform") version "1.3.70"
    id("maven-publish")
}

repositories {
    mavenCentral()
    //maven(url = "https://kotlin.bintray.com/kotlinx")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

group = "com.github.reitzig"
version = "0.0.1"

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                // Setup the Kotlin compiler options for the 'main' compilation:
                jvmTarget = "11"
            }

            compileKotlinTask // get the Kotlin task 'compileKotlinJvm'
            output // get the main compilation output
        }

        compilations["test"].runtimeDependencyFiles // get the test runtime classpath
    }

    //js()
    //linuxX64("linux")

    sourceSets {
        all {
            languageSettings.apply {
                languageVersion = "1.3"
                apiVersion = "1.3"
                enableLanguageFeature("InlineClasses")
                progressiveMode = true
            }
        }

        val commonKoparj by creating {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonKoparjTest by creating {
            dependsOn(commonKoparj)

            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val commonMain by getting {
            dependsOn(commonKoparj)

            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependsOn(commonKoparjTest)

            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
        // JVM-specific tests and their dependencies:
        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
    }
}
