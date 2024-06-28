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
    // Apache v5:
    implementation("org.http4k:http4k-server-apache")

    // Apache v4:
    implementation("org.http4k:http4k-server-apache4")

    // Jetty & JettyLoom:
    implementation("org.http4k:http4k-server-jetty")

    // Helidon (Loom):
    implementation("org.http4k:http4k-server-helidon")

    // Ktor CIO:
    implementation("org.http4k:http4k-server-ktorcio")

    // Ktor Netty:
    implementation("org.http4k:http4k-server-ktornetty")

    // Netty:
    implementation("org.http4k:http4k-server-netty")

    // Ratpack:
    implementation("org.http4k:http4k-server-ratpack")

    // Undertow:
    implementation("org.http4k:http4k-server-undertow")

    // Java WebSocket:
    implementation("org.http4k:http4k-server-websocket")
    implementation("org.http4k:http4k-client-apache")
    implementation("org.http4k:http4k-multipart")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation(kotlin("reflect"))
    implementation("org.jboss.xnio:xnio-nio:3.8.16.Final")
    implementation ("com.andreapivetta.kolor:kolor:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}