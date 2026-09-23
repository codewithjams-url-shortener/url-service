import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.25.0"
}

group = "io.url-shortener"
version = "0.1.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	mavenLocal()
}

dependencyManagement {
	imports {
		mavenBom("org.testcontainers:testcontainers-bom:1.21.4")
	}
}

// Deliberately isolated from `main`: this source set has no compile-time dependency on url-service's
// own classes (no UrlServiceApplication, no generated DTOs, no AwsProperties/AwsConstants). It treats
// url-service purely as an HTTP black box - "local" launches the real built jar as a separate OS
// process (see LocalEnvironmentConfig), "deployed" just points at a configured URL - and talks JSON
// over the wire rather than sharing Java types with the app it's testing.
sourceSets {
	create("integrationTest")
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("io.micrometer:micrometer-tracing-bridge-otel")
	implementation("io.opentelemetry:opentelemetry-exporter-otlp")
	implementation("software.amazon.awssdk:dynamodb:2.54.13")
	implementation("io.url-shortener:service-common:0.1.0")
	implementation("io.url-shortener:links-contract:0.1.0")
	implementation("com.google.zxing:core:3.5.3")
	implementation("com.google.zxing:javase:3.5.3")

	// Required by openapi-generator's "spring" output (interfaceOnly): the generated API
	// interface uses Swagger's OpenAPI 3 annotations, and generated models use JsonNullable
	// for optional properties.
	implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.55")
	implementation("org.openapitools:jackson-databind-nullable:0.2.11")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	compileOnly("org.projectlombok:lombok:1.18.48")
	annotationProcessor("org.projectlombok:lombok:1.18.48")

	"integrationTestImplementation"("org.springframework.boot:spring-boot-starter-web")
	"integrationTestImplementation"("org.springframework.boot:spring-boot-starter-test")
	"integrationTestImplementation"("software.amazon.awssdk:dynamodb:2.54.13")
	"integrationTestImplementation"("io.floci:testcontainers-floci:1.15.0")
	"integrationTestImplementation"("org.testcontainers:junit-jupiter")
	"integrationTestRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register<Test>("localIntegrationTest") {
	description = "Runs the shared integration tests with a Testcontainers-backed floci instance and " +
			"the built app launched as a separate process (spring.profiles.active=local)."
	group = "verification"
	dependsOn(tasks.named("bootJar"))
	testClassesDirs = sourceSets["integrationTest"].output.classesDirs
	classpath = sourceSets["integrationTest"].runtimeClasspath
	systemProperty("spring.profiles.active", "local")
	systemProperty("integration-test.app-jar", tasks.named<BootJar>("bootJar").get().archiveFile.get().asFile.path)
}

tasks.register<Test>("integrationTest") {
	description = "Runs the shared integration tests against an already-deployed url-service instance " +
			"(spring.profiles.active=deployed). Pass the target with -PbaseUrl=https://...."
	group = "verification"
	testClassesDirs = sourceSets["integrationTest"].output.classesDirs
	classpath = sourceSets["integrationTest"].runtimeClasspath
	systemProperty("spring.profiles.active", "deployed")
	systemProperty("integration-test.base-url", (project.findProperty("baseUrl") as String?) ?: "")
}

openApiGenerate {
	val basePackage = "io.urlshortener.urlservice"
	generatorName.set("spring")
	inputSpec.set("$projectDir/src/main/resources/openapi/openapi.yaml")
	outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.path)
	apiPackage.set("$basePackage.controller")
	modelPackage.set("$basePackage.model.dataTransferObject")
	configOptions.apply {
		put("interfaceOnly", "true")
		put("useSpringBoot3", "true")
		put("generateBuilders", "true")
	}
}

sourceSets.main {
	java.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/java"))
}

tasks.compileJava {
	dependsOn(tasks.openApiGenerate)
}
