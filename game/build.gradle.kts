plugins {
    application
}

val annotationsVersion: String by rootProject.extra

repositories {
    mavenCentral()
}

application {
    mainClass = "io.github.animaexinani.game.NetworkedGame"
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    implementation(project(":engine"))
    compileOnly(libs.jetbrains.annotations)
}
