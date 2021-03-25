plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.1.1"
  kotlin("plugin.spring") version "1.4.30"
  kotlin("plugin.jpa") version "1.3.61"
  idea
}

configurations {
  implementation { exclude(group = "tomcat-jdbc") }
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

  runtimeOnly("com.h2database:h2:1.4.200")
  runtimeOnly("org.flywaydb:flyway-core:7.5.3")
  runtimeOnly("org.postgresql:postgresql:42.2.19")

  implementation("javax.transaction:javax.transaction-api:1.3")
  implementation("javax.xml.bind:jaxb-api:2.3.1")
  implementation("com.google.code.gson:gson:2.8.6")

  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.1")

  implementation("org.springdoc:springdoc-openapi-webmvc-core:1.5.4")
  implementation("org.springdoc:springdoc-openapi-ui:1.5.4")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.5.4")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.5.4")

  implementation(platform("com.amazonaws:aws-java-sdk-bom:1.11.958"))
  implementation("com.amazonaws:aws-java-sdk-sns:1.11.958")

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.24.0")
  testImplementation("org.testcontainers:localstack:1.15.2")
  testImplementation("org.awaitility:awaitility-kotlin:4.0.3")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.4.2")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("org.mockito:mockito-inline:3.7.7")
}
