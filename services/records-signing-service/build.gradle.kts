plugins {
	java
	id("org.springframework.boot") version "3.0.6"
	id("io.spring.dependency-management") version "1.1.0"
}

group = "ua.sinaver.web3"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_19

repositories {
	mavenCentral()
}

dependencies {
	implementation ("org.springframework.boot:spring-boot-starter-amqp")
	implementation ("org.springframework.boot:spring-boot-starter-batch")
	implementation ("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation ("org.springframework.boot:spring-boot-starter-web")
	
	developmentOnly ("org.springframework.boot:spring-boot-devtools")
	
	runtimeOnly ("com.h2database:h2")
	testImplementation ("org.springframework.boot:spring-boot-starter-test")
	testImplementation ("org.springframework.amqp:spring-rabbit-test")
	testImplementation ("org.springframework.batch:spring-batch-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
