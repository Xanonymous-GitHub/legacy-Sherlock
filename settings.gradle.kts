pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings

    plugins {
        `kotlin-dsl`
        kotlin("jvm") version kotlinVersion apply false
        kotlin("plugin.spring") version kotlinVersion apply false
        kotlin("plugin.jpa") version kotlinVersion apply false
        id("org.springframework.boot") version springBootVersion apply false
    }
}

rootProject.name = "Sherlock"

includeBuild("gumtree")
