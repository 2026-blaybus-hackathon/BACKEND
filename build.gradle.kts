plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.2.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
    id("com.google.cloud.tools.jib") version "3.4.0"
}
jib {
    from {
        image = "eclipse-temurin:21-jre"
        platforms {
            platform {
                os = "linux"
                architecture = "arm64"
            }
        }
    }
    to {
        image = "chaechae121/blaybus:latest"
        auth {
            username = System.getenv("DOCKER_USERNAME") ?: ""
            password = System.getenv("DOCKER_PASSWORD") ?: ""
        }
    }
}
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

group = "com.blaybus"
version = "0.0.1-SNAPSHOT"
description = "2026-blaybus-hackerton-backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // Source: https://mvnrepository.com/artifact/org.redisson/redisson-spring-boot-starter
    implementation("org.redisson:redisson-spring-boot-starter:4.1.0")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // thyemleaf
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // .env support
    implementation("io.github.cdimascio:java-dotenv:5.2.2")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Oracle DB
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")
    implementation("com.oracle.database.security:oraclepki:23.26.0.0.0")
    implementation("com.oracle.database.security:osdt_core:21.20.0.0")
    implementation("com.oracle.database.security:osdt_cert:21.20.0.0")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("com.oracle.oci.sdk:oci-java-sdk-objectstorage:3.14.0")
    implementation("com.oracle.oci.sdk:oci-java-sdk-common-httpclient-jersey3:3.14.0")

    implementation("com.ibm.icu:icu4j:74.2")
    implementation("io.sentry:sentry-jdbc:8.22.0")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
