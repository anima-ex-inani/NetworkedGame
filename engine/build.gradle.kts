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
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.platform)
}

tasks.test {
    useJUnitPlatform()
}
