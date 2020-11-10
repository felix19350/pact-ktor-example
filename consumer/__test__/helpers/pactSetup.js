const path = require("path")
const Pact = require("@pact-foundation/pact").Pact

global.port = 8081
global.provider = new Pact({
	port: global.port,
	log: path.resolve(process.cwd(), "__test__/contract/logs", "logs-pact.log"),
	dir: path.resolve(process.cwd(), "__test__/contract/pacts"),
	spec: 3, // JVM 3 , other 2
	logLevel: "INFO",
	pactfileWriteMode: "overwrite",
	consumer: "ClientsFrontend",
	provider: "ClientsApi"
})
