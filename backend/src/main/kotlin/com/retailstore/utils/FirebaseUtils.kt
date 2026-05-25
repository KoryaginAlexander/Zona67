package com.retailstore.utils

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import java.io.FileInputStream

object FirebaseUtils {
    fun initialize(credentialsPath: String) {
        if (FirebaseApp.getApps().isNotEmpty()) return
        val file = java.io.File(credentialsPath)
        if (!file.exists() || !file.isFile) {
            println("WARNING: Firebase credentials not found at $credentialsPath — auth will fail")
            return
        }
        val credentials = GoogleCredentials.fromStream(FileInputStream(credentialsPath))
        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build()
        FirebaseApp.initializeApp(options)
    }

    fun verifyIdToken(idToken: String): FirebaseTokenData {
        val decoded = FirebaseAuth.getInstance().verifyIdToken(idToken)
        return FirebaseTokenData(
            uid = decoded.uid,
            email = decoded.email ?: ""
        )
    }
}

data class FirebaseTokenData(val uid: String, val email: String)
