plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    application
}

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.4"
val exposedVersion = "0.41.1"

dependencies {
    // ── Ktor Core ─────────────────────────────────────────────────────────
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-request-validation:$ktorVersion")

    // ── JWT & OAuth ───────────────────────────────────────────────────────
    implementation("com.auth0:java-jwt:4.4.0")

    // ── Encryption & Security ─────────────────────────────────────────────
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")        // Crypto provider
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")        // X.509 certificates
    implementation("org.mindrot:jbcrypt:0.4")                     // Password hashing
    implementation("commons-codec:commons-codec:1.16")            // Base64, Hex
    
    // ── Digital Signatures & Certificates ──────────────────────────────────
    implementation("org.apache.commons:commons-lang3:3.13.0")
    implementation("commons-io:commons-io:2.13.0")
    
    // ── HTTP Signing (for blockchain/verification) ────────────────────────
    implementation("com.auth0:java-jwt:4.4.0")
    
    // ── Hashing & Integrity ───────────────────────────────────────────────
    // (javax.crypto is part of Java runtime, but includes for clarity)

    // ── Database ──────────────────────────────────────────────────────────
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.flywaydb:flyway-core:9.22.0")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // ── Logging ──────────────────────────────────────────────────────────
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // ── Serialization ────────────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // ── Testing ──────────────────────────────────────────────────────────
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("junit:junit:4.13.2")
}

application {
    mainClass.set("org.psei.ApplicationKt")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}
