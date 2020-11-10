package mobi.waterdog.pact.example

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.netty.EngineMain
import kotlinx.serialization.json.Json

fun Application.main() {
    install(ContentNegotiation) {
        json(
            contentType = ContentType.Application.Json,
            json = Json {
                encodeDefaults = false
                ignoreUnknownKeys = false
                isLenient = false
                allowStructuredMapKeys = false
                prettyPrint = false
                coerceInputValues = false
                classDiscriminator = "type"
                allowSpecialFloatingPointValues = false
            }
        )
    }
    routing {
        clientApi()
    }

    // This is important for the gradle spawn plugin to work.
    // See: https://github.com/gradle/gradle/issues/1367
    println("Started Application")
}

fun main(args: Array<String>) {
    EngineMain.main(args)
}