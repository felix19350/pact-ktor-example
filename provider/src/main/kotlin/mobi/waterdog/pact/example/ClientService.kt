package mobi.waterdog.pact.example

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class ClientService {

    companion object {
        private val clients: ConcurrentHashMap<Long, Client> = ConcurrentHashMap()
        private val nextId: AtomicLong = AtomicLong(0)
    }

    init {
        createClient(
            CreateClientCommand(
                "Lisa", "Simpson", 8
            )
        )
        createClient(
            CreateClientCommand(
                "Wonder", "Woman", 30
            )
        )
        createClient(
            CreateClientCommand(
                "Homer", "Simpson", 39
            )
        )
    }

    fun getClients() = clients.values.toList()

    fun getClient(id: Long) = clients[id]

    fun createClient(cmd: CreateClientCommand): Client {
        val nextId = nextId.incrementAndGet()
        val client = cmd.toClient(nextId)
        clients[nextId] = client
        return client
    }
}