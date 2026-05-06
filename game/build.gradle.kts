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
    implementation("org.dyn4j:dyn4j:5.0.2")
    compileOnly(libs.jetbrains.annotations)
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    modularity.inferModulePath.set(false)
}
