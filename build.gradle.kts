import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")

    id("org.springframework.boot")

    id("io.spring.dependency-management") version "1.1.4"
    id("antlr")
    id("war")
}

repositories {
    mavenCentral()
    google()
    maven("https://m2.objectdb.com")
}

internal val kotlinVersion: String by rootProject.extra
internal val springBootVersion: String by rootProject.extra
internal val buildLocation: String = layout.buildDirectory.get().toString()

group = "uk.ac.warwick.dcs.sherlock"
version = "release"
description = """Sherlock"""

apply(plugin = "kotlin")
apply(plugin = "project-report")

java {
    val javaVersion = JavaVersion.VERSION_21
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.majorVersion))
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-mail:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-log4j2:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-security:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")

    implementation("org.flywaydb:flyway-core:10.11.1")
    implementation("jakarta.validation:jakarta.validation-api:3.1.0-M1")
    implementation("org.antlr:antlr4-runtime:4.13.1")
    implementation("com.google.guava:guava:33.1.0-jre")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("commons-io:commons-io:2.16.1")
    implementation("commons-codec:commons-codec:1.16.1")
    implementation("org.eclipse.persistence:org.eclipse.persistence.jpa:4.0.2")
    implementation("javax.transaction:jta:1.1")
    implementation("com.objectdb:objectdb:2.8.9_07")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.1.2.RELEASE")
    implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("javax.xml.bind:jaxb-api")
    implementation("org.json:json:20240303")
    implementation("org.seleniumhq.selenium:selenium-java:4.19.1")
    implementation("com.h2database:h2:2.2.224")

    runtimeOnly("com.mysql:mysql-connector-j:8.3.0")

    if (project.gradle.startParameter.taskNames.contains("bootWar")) {
        providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
    }

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")

    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    antlr("org.antlr:antlr4:4.13.1")
}

configurations {
    configureEach {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

tasks {
    withType<KotlinJvmCompile>().configureEach {
        dependsOn(generateGrammarSource)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
        }
    }

    register("printVersion") {
        doLast {
            println("Version: ${project.version}")
        }
    }

    register("deps", Copy::class) {
        from(configurations.runtimeOnly)
        into("$buildLocation/out/lib")
    }

    javadoc {
        source = fileTree("src/main/java")
        setDestinationDir(file("${projectDir}/docs"))
    }

    bootJar {
        destinationDirectory.set(file("$buildLocation/out"))
        mainClass = "uk.ac.warwick.dcs.sherlock.launch.SherlockClient"
        delete {
            fileTree("$buildLocation/out") {
                include("*.jar")
                exclude(archiveFileName.toString())
                exclude(archiveFile.get().asFile.name)
                exclude("*-dev.jar")
            }
        }
    }

    jar {
        dependsOn(bootJar)

        from(sourceSets.main.get().output)
        include("uk/ac/warwick/dcs/sherlock/api/**")
        include("uk/ac/warwick/dcs/sherlock/module/model/base/**")

        enabled = true
        destinationDirectory.set(file("$buildLocation/out"))
        archiveClassifier.set("dev")

        delete {
            fileTree("$buildLocation/out") {
                include("*-dev.jar")
                exclude(archiveFileName.toString())
                exclude(archiveFile.get().asFile.name)
            }
        }
    }

    assemble {
        dependsOn(jar)
    }

    bootWar {
        destinationDirectory.set(file("$buildLocation/out"))
        mainClass = "uk.ac.warwick.dcs.sherlock.launch.SherlockServer"
        delete {
            fileTree("$buildLocation/out") {
                include("*.war")
                exclude(archiveFileName.toString())
                exclude(archiveFile.get().asFile.name)
            }
        }
    }

    named("war") {
        dependsOn(bootJar)
    }

    bootRun {
        mainClass = "uk.ac.warwick.dcs.sherlock.launch.SherlockClient"
        jvmArgs = listOf("-Dspring.profiles.active=dev", "-Dspring.output.ansi.enabled=ALWAYS")
    }
}
