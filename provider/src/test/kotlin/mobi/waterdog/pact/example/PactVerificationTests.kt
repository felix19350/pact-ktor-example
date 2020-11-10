package mobi.waterdog.pact.example

import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.junitsupport.target.TestTarget
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith

@Provider("ClientsApi")
@PactBroker(host = "localhost", port = "9292", scheme = "http")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PactVerificationTests {

    private lateinit var sut: ApplicationEngine

    @TestTarget
    private val target = HttpTestTarget(host="localhost", port=8080, path="/")

    @BeforeAll
    fun setupApp(){
        sut = embeddedServer(Netty, port = 8080){
            main()
        }
        sut.start(wait=false)
        Thread.sleep(50)
    }

    @AfterAll
    fun tearDownApp(){
        sut.stop(1000, 2000)
    }

    @State("i have a list of clients")
    fun listOfClients(){

    }

    @State("i create a new client")
    fun newClient(){}

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }


}
