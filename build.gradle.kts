plugins {
    kotlin("jvm") version "2.0.21"
}

group = "net.jeikobu.mqtt-status"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.davidepianca98:kmqtt-common:0.4.8")
    implementation("io.github.davidepianca98:kmqtt-client:0.4.8")
    implementation("com.sksamuel.hoplite:hoplite-core:2.9.0")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.9.0")
    implementation("org.tinylog:tinylog-api-kotlin:2.7.0")
    implementation("org.tinylog:tinylog-impl:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "net.jeikobu.mqttstatus.MainKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val sourcesMain = sourceSets.main.get()
    val contents = configurations.runtimeClasspath.get()
        .map { if (it.isDirectory) it else zipTree(it) } + sourcesMain.output
    from(contents)
}

kotlin {
    jvmToolchain(21)
}
