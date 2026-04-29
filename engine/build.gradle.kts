plugins {
    `java-library`
}

val lwjglNatives: String by rootProject.extra

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    implementation(libs.bundles.lwjgl)
    runtimeOnly(libs.bundles.lwjgl) {
        artifact {
            classifier = lwjglNatives
        }
    }
    compileOnly(libs.jetbrains.annotations)
    implementation("org.dyn4j:dyn4j:5.0.2")
}
