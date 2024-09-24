plugins {
    kotlin("jvm") version "1.9.23"
    `java-library`
}

group = "org.aquiles"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    testImplementation(kotlin("test"))


    implementation("com.google.code.gson:gson:2.8.9")
    implementation(kotlin("reflect"))
    implementation("org.jboss.xnio:xnio-nio:3.8.16.Final")
    implementation ("com.andreapivetta.kolor:kolor:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")

    // https://mvnrepository.com/artifact/io.undertow/undertow-core
    implementation("io.undertow:undertow-core:2.3.14.Final")

    implementation("commons-fileupload:commons-fileupload:1.4")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}