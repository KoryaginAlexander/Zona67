Yewpackage com.retailstore

import com.retailstore.di.appModule
import com.retailstore.plugins.*
import com.retailstore.utils.FirebaseUtils
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    val credentialsPath = environment.config.propertyOrNull("firebase.credentialsPath")?.getString()
        ?: System.getenv("FIREBASE_CREDENTIALS_PATH")
        ?: "firebase-credentials.json"
    FirebaseUtils.initialize(credentialsPath)

    configureSerialization()
    configureSecurity()
    configureDatabase()
    configureRouting()
}
