plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.2.1"
  kotlin("plugin.spring") version "1.5.10"
  kotlin("plugin.jpa") version "1.5.10"
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
  implementation("org.springframework.cloud:spring-cloud-starter-aws-messaging:2.2.6.RELEASE")

  implementation("org.flywaydb:flyway-core:7.9.0")
  runtimeOnly("org.postgresql:postgresql:42.2.20")
  implementation("com.vladmihalcea:hibernate-types-52:2.11.1")

  implementation("javax.transaction:javax.transaction-api:1.3")
  implementation("javax.xml.bind:jaxb-api:2.3.1")
  implementation("com.google.code.gson:gson:2.8.6")

  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.3")

  implementation("org.springdoc:springdoc-openapi-webmvc-core:1.5.8")
  implementation("org.springdoc:springdoc-openapi-ui:1.5.8")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.5.8")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.5.8")

  implementation(platform("com.amazonaws:aws-java-sdk-bom:1.11.1020"))
  implementation("com.amazonaws:aws-java-sdk-sns:1.11.1020")

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.25.0")
  testImplementation("org.testcontainers:localstack:1.15.3")
  testImplementation("org.testcontainers:postgresql:1.15.3")
  testImplementation("org.awaitility:awaitility-kotlin:4.1.0")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.5.10")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("org.mockito:mockito-inline:3.10.0")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "16"
  }
}
