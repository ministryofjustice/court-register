plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.1.5-beta-1"
  kotlin("plugin.spring") version "1.6.21"
  kotlin("plugin.jpa") version "1.6.21"
  idea
}

configurations {
  implementation { exclude(group = "tomcat-jdbc") }
  implementation { exclude(module = "spring-boot-graceful-shutdown") }
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")

  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:1.1.3")
  implementation("org.springframework.cloud:spring-cloud-starter-aws-messaging:2.2.6.RELEASE")

  implementation("org.flywaydb:flyway-core:8.5.8")
  runtimeOnly("org.postgresql:postgresql:42.3.4")
  implementation("com.vladmihalcea:hibernate-types-52:2.16.1")

  implementation("javax.transaction:javax.transaction-api:1.3")
  implementation("javax.xml.bind:jaxb-api:2.3.1")
  implementation("com.google.code.gson:gson:2.9.0")

  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")

  implementation("org.springdoc:springdoc-openapi-webmvc-core:1.6.7")
  implementation("org.springdoc:springdoc-openapi-ui:1.6.7")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.7")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.6.7")

  implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.202"))

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.34.0")
  testImplementation("org.testcontainers:localstack:1.17.1")
  testImplementation("org.testcontainers:postgresql:1.17.1")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.1")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("org.mockito:mockito-inline:4.5.0")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }
}
