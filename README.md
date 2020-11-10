# pact-ktor-example
Project that illustrates how to use contract testing with pact w/ ktor

## What am I getting?

This is an example project illustrating how to use contract testing ([pact](https://pact.io)) with a javascript consumer
and a ktor provider. This represents the common setup where there is a frontend consuming a REST-like API (JS consumer) and a
backend API (the provider).

### Contract testing with pact
_Contract testing is a technique for testing an integration point by checking each application in isolation to ensure the
 messages it sends or receives conform to a shared understanding documented in a "contract."_ - [Pact docs](https://docs.pact.io/)

In general pact works by asserting that the calls to the test doubles, return the same result as a call to the real 
application would. In practical terms, this means that:

1. The consumer (JS client) runs its tests and generates a contract document;
2. The consumer publishes that contract somewhere, where the provider can fetch it. In our case we will be using the 
[pact broker](https://docs.pact.io/pact_broker/);
3. The provider is connected to the broker, and the contract is used to assert that the responses match the expectations
of the client;
4. The provider publishes its results back to the broker, giving the operators visibility whether the updates to the API
break the consumer's expectations.

This is an interesting tool because it allows developers to cover a broad range of scenarios that are traditionally covered
by expensive integration tests requiring the presence of the consumer and producer at the same time.

## Getting started

### Requirements:
* Docker
* Docker compose
* NPM
* JDK 11 or above

### 1 - Start the broker:
In the `/broker` folder there is a compose file (for docker compose)  that allows you to launch an instance of the pact 
broker, with the following command:

```shell script
docker-compose -f pact-broker.yml up
```

You can verify that the broker is up by opening the browser on [http://localhost:9292](http://localhost:9292)
For further information please check the [pact broker documentation](https://docs.pact.io/pact_broker/)

### 2 - Run the consumer tests
The `/consumer` folder contains a sample javascript client, that will consume our API. The plain old jest tests in the 
`/consumer/__test__/contract` folder exercise the actual calls to the API defined in `/consumer/src/consumer.js`.

You can find more information about the specific pact features used in the tests [here](https://docs.pact.io/implementation_guides/javascript/readme#consumer-side-testing)

In order to run the consumer tests, you need to run the following commands:
```shell script
npm install
npm run test:consumer
```

You'll note that there is a new json file under `/consumer/__test__/contract/pacts`, this is our contract, which we will now
publish to the pact broker, with the following command (you'll need the broker running):
```shell script
npm run publish:contract
```

In order to get started with the tests on the consumer, there's a bit of boilerplate code at play, the `/consumer/__test__/helpers` takes care of that:
 
* pactSetup.js - Setup the port of the mock server and several settings regarding the generation of the contract. In particular the consumer and provider names.
* pactTestWrapper.js - starts and stops the mock server before and after each test respectively.
* publish.js - publishes the generated contract to the broker

On the `package.json` its also good to look at the `test:consumer` and `publish:contract` scripts.
After publishing the contract, 

The consumer is adapted from this [Test Automation University course](https://github.com/rafaelaazevedo/tau-pact-nodejs-course)

### 3 - Run the provider tests

For the provider, we've implemented two different ways of running the pact verification: using the [gradle task](https://docs.pact.io/implementation_guides/jvm/provider/gradle/)
and using the [junit 5 extension](https://docs.pact.io/implementation_guides/jvm/provider/junit5).

In either case, the broker needs to be running in order for the provider to fetch the contract. 

#### Gradle
In order to run with gradle, you can use the following commands (in the provider folder):
```shell script
gradlew pactVerify  # verify the contract
gradlew pactPublish # publish the results back to the broker
```

#### Junit 5
The Junit 5 integration simply runs the pact verification as part of the normal test suite. So just run_
```shell script
gradlew test
```
