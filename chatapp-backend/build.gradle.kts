import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.buildpack.platform.docker.type.ImageName
import org.springframework.boot.buildpack.platform.docker.type.ImageReference
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("org.springframework.boot") version "3.0.5"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0"
    kotlin("plugin.jpa") version "1.8.0"
    id("org.graalvm.buildtools.native") version "0.9.20" // Necessary with JPA
    war
}

group = "es.unizar.mii.tmdad"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.passay:passay:1.6.3")
    implementation("com.fasterxml.uuid:java-uuid-generator:4.1.1")

    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springframework.security:spring-security-messaging")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-config")

    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.rabbitmq:http-client:5.0.0")

    implementation("com.jlefebure:spring-boot-starter-minio:1.10")
    implementation("io.minio:minio:8.5.2")
    implementation("org.apache.commons:commons-io:1.3.2")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.4")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<BootBuildImage>("bootDebugBuildImage") {
    group="build"
    environment.set(mapOf(
        "BPL_DEBUG_ENABLED" to "true"
    ))
    verboseLogging.set(true)
    dependsOn(tasks.bootWar)
    archiveFile.set(tasks.bootWar.get().archiveFile.get().asFile)
    imageName.set(ImageReference.of(ImageName.of(project.name),"${project.version}-debug").toString())
}
