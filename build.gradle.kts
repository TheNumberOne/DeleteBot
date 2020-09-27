import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    id("io.spring.dependency-management") version "1.0.7.RELEASE"
    application
}

group = "me.rose"
version = "1.0-SNAPSHOT"
dependencyManagement {
    imports {
        mavenBom("io.projectreactor:reactor-bom:Dysprosium-SR12")
    }
}

repositories {
    mavenCentral()
}
dependencies {
    testImplementation(kotlin("test-junit5"))
    implementation("com.discord4j:discord4j-core:3.1.1")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("io.projectreactor:reactor-tools")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.3.9")

}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
application {
    mainClassName = "MainKt"
}