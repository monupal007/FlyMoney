package com.laundryapp.backend

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.laundryapp.shared.models.NotificationRequest
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")
    
    install(ContentNegotiation) {
        json()
    }

    // Initialize Firebase Admin
    val serviceAccountPath = "service-account.json"
    val serviceAccountFile = File(serviceAccountPath)
    
    if (serviceAccountFile.exists()) {
        try {
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(FileInputStream(serviceAccountFile)))
                .build()
            
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                logger.info("Firebase Admin SDK initialized successfully")
            }
        } catch (e: Exception) {
            logger.error("Error initializing Firebase Admin SDK: ${e.message}")
        }
    } else {
        logger.error("Firebase service-account.json not found at ${serviceAccountFile.absolutePath}")
        logger.error("Please place your Firebase service account JSON file in the backend directory.")
    }

    routing {
        get("/") {
            call.respondText("Laundry App Ktor Backend is Running")
        }

        post("/send-notification") {
            try {
                val request = call.receive<NotificationRequest>()
                
                val message = Message.builder()
                    .setToken(request.targetToken)
                    .setNotification(
                        Notification.builder()
                            .setTitle(request.title)
                            .setBody(request.body)
                            .build()
                    )
                    .putAllData(request.data ?: emptyMap())
                    .build()

                val response = FirebaseMessaging.getInstance().send(message)
                call.respond(mapOf("status" to "Success", "messageId" to response))
            } catch (e: Exception) {
                call.respond(mapOf("status" to "Error", "reason" to e.message))
            }
        }
    }
}
