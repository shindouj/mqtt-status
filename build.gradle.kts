plugins {
    kotlin("jvm") version "2.1.0"
    id("com.palantir.git-version") version "3.1.0"
}

val gitVersion: groovy.lang.Closure<String> by extra
group = "net.jeikobu.mqtt-status"
version = gitVersion()

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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    testImplementation(kotlin("test"))
    testImplementation("io.github.davidepianca98:kmqtt-broker:0.4.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
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
