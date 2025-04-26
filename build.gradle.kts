val kotlin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val h2_version: String by project

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.ktor.plugin") version "3.1.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
    id("com.google.devtools.ksp") version "2.0.20-1.0.24"
}

group = "com.example"
version = "1.5.2"

application {
    mainClass.set("com.example.ApplicationKt")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
    maven("https://jitpack.io")
}

tasks {
    jar {
        enabled = false
    }
    shadowJar {
        archiveFileName.set("ERAkt-${project.version}.jar")
        mergeServiceFiles()
    }
}

val komapperVersion = "5.2.1"

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-websockets-jvm")
    implementation("io.ktor:ktor-server-rate-limit-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation("io.ktor:ktor-network-tls-certificates-jvm")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")
    platform("org.komapper:komapper-platform:$komapperVersion").let {
        implementation(it)
        ksp(it)
    }
    implementation("org.komapper:komapper-starter-r2dbc:$komapperVersion")
    runtimeOnly("org.komapper:komapper-datetime-r2dbc:$komapperVersion")
    runtimeOnly("org.komapper:komapper-dialect-postgresql-r2dbc")
    ksp("org.komapper:komapper-processor")
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.akuleshov7:ktoml-core:0.5.1")
}