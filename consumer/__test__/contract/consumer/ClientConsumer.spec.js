"use strict"

const { Matchers } = require("@pact-foundation/pact")
const { getClients, postClient } = require("../../../src/consumer")

describe("Clients Service", () => {

    afterEach(() => provider.verify())

    describe("GET Clients", () => {

        const GET_EXPECTED_BODY = require("../../../src/data/clientData.json")

        beforeEach(() => {
            const interaction = {
                state: "i have a list of clients",
                uponReceiving: "a request for all clients",
                withRequest: {
                    method: "GET",
                    path: "/clients",
                    headers: {
                        Accept: "application/json, text/plain, */*"
                    },
                },
                willRespondWith: {
                    status: 200,
                    headers: {
                        "Content-Type": "application/json; charset=utf-8"
                    },
                    body: GET_EXPECTED_BODY
                }
            }

            return provider.addInteraction(interaction)
        })

        test("returns correct body, header and statusCode", async () => {
            const response = await getClients()
            expect(response.headers['content-type']).toBe("application/json; charset=utf-8")
            expect(response.data).toEqual(GET_EXPECTED_BODY)
            expect(response.status).toEqual(200)
        })
    })

    describe("POST Clients", () => {
        const POST_BODY = {
            firstName: "Jonh",
            lastName: "Doe",
            age: 101
        }

        const POST_EXPECTED_BODY = Matchers.like({
            firstName: POST_BODY.firstName,
            lastName: POST_BODY.lastName,
            age: POST_BODY.age,
            id: 3,
        })

        beforeEach(() => {
            const interaction = {
                state: "i create a new client",
                uponReceiving: "a request to create client with firstname and lastname",
                withRequest: {
                    method: "POST",
                    path: "/clients",
                    headers: {
                        "Content-Type": "application/json;charset=utf-8"
                    },
                    body: POST_BODY
                },
                willRespondWith: {
                    status: 200,
                    body: Matchers.like(POST_EXPECTED_BODY).contents
                }
            }

            return provider.addInteraction(interaction)
        })

        test("returns correct body, header and statusCode", async () => {
            const response = await postClient(POST_BODY);
            expect(response.data.id).toEqual(3)
            expect(response.status).toEqual(200)
        })
    })
})