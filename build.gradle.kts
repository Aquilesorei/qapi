plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.aquiles"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    testImplementation(kotlin("test"))
  //  implementation(platform("org.http4k:http4k-bom:5.17.0.0"))
    implementation("org.http4k:http4k-core")

    // Undertow:
    implementation("org.http4k:http4k-server-undertow")

    // Java WebSocket:
    implementation("org.http4k:http4k-server-websocket")
    implementation("org.http4k:http4k-client-apache")
    implementation("org.http4k:http4k-multipart")
    implementation("org.http4k:http4k-contract")
    implementation("org.http4k:http4k-format-jackson")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation(kotlin("reflect"))
    implementation("org.jboss.xnio:xnio-nio:3.8.16.Final")
    implementation ("com.andreapivetta.kolor:kolor:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")

    // https://mvnrepository.com/artifact/io.undertow/undertow-core
    implementation("io.undertow:undertow-core:2.3.14.Final")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}