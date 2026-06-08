plugins {
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "com.laundryapp"
version = "1.0.0"

application {
    mainClass.set("com.laundryapp.backend.ApplicationKt")
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    
    implementation(libs.firebase.admin)
    implementation(libs.logback.classic)
}
