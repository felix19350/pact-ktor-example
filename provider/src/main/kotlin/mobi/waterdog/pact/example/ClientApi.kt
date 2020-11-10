package mobi.waterdog.pact.example

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.title

fun Route.clientApi() {

    val clientService = ClientService()

    get("/") {
        call.respondHtml(HttpStatusCode.OK) {
            head {
                title("Pact demo application.")
            }
            body {
                div {
                    +"Hello from Ktor"
                }
            }
        }
    }

    get("/clients") {
        val clients = clientService.getClients()
        call.respond(HttpStatusCode.OK, clients)
    }

    get("/clients/{id}") {
        val clientId = call.parameters["id"]?.toLongOrNull()
        requireNotNull(clientId) { "The client id should be a valid number" }

        val result = clientService.getClient(clientId)
        if (result != null) {
            call.respond(HttpStatusCode.OK, result)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    post("/clients"){
        try {
            val cmd: CreateClientCommand = call.receive()
            val errors = cmd.validate()
            if (errors.isNotEmpty()) {
                call.respond(HttpStatusCode.BadRequest, errors)
            }

            val result = clientService.createClient(cmd)
            call.respond(HttpStatusCode.OK, result)
        }catch(ex:Exception){
            ex.printStackTrace()
        }
    }
}