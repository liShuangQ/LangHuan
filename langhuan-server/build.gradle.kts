plugins {
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.9.25"
    kotlin("kapt") version "1.9.25"
}

group = "com"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["springAiVersion"] = "1.0.3"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Database
    implementation("org.postgresql:postgresql:42.7.4")

    // Spring AI
    implementation("org.springframework.ai:spring-ai-pgvector-store")
    implementation("org.springframework.ai:spring-ai-tika-document-reader")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Storage
    implementation("io.minio:minio:8.4.0") {
        exclude(group = "org.apache.commons", module = "commons-compress")
    }
    implementation("org.apache.commons:commons-compress:1.26.1")

    // Document Processing
    implementation("org.apache.poi:poi-ooxml:5.2.3") {
        exclude(group = "commons-io", module = "commons-io")
    }

    // JWT
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Utilities
    implementation("cn.hutool:hutool-all:5.8.34")
    implementation("commons-collections:commons-collections:3.2.2")
    implementation("com.baomidou:mybatis-plus-spring-boot3-starter:3.5.7")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    kapt("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Lucene
    implementation("org.apache.lucene:lucene-core:9.11.1")
    implementation("org.apache.lucene:lucene-analyzers-common:8.11.1")
    implementation("org.apache.lucene:lucene-queryparser:9.11.1")

    // HanLP
    implementation("com.hankcs:hanlp:portable-1.8.6")

    // Test Dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Spring Boot configuration
springBoot {
    mainClass = "com.langhuan.LanghuanApplicationKt"
}

// Enable Lombok support for Kotlin
kapt {
    keepJavacAnnotationProcessors = true
}

// Configure the jar task
tasks.jar {
    enabled = false
}

tasks.bootJar {
    enabled = true
    archiveClassifier.set("")
}