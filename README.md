# pact-ktor-example
Project that illustrates how to use contract testing with pact w/ a ktor backend.

## What am I getting?

This is an example project illustrating how to use contract testing ([pact](https://pact.io)) with a javascript consumer
and a ktor provider. This represents the common setup where there is a frontend consuming a REST-like API (JS consumer) and a
backend API (the provider).

The backend is a very minimal [ktor](https://ktor.io) API. Some working knowledge of the framework is recommended.

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

## Usage

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

__Behind the scenes:__

With Gradle, we are using the pact [gradle plugin](https://plugins.gradle.org/plugin/au.com.dius.pact). It adds a series of
tasks, namely the `pactVerify` and `pactPublish` tasks above.

```groovy
pact {
    // Turn on reporting: https://github.com/pact-foundation/pact-jvm/tree/master/provider/gradle#verification-reports
    reports {
        defaultReports()
        json
    }

    // Add the broker where the contracts are stored: https://github.com/pact-foundation/pact-jvm/tree/master/provider/gradle#verification-reports
    broker{
        pactBrokerUrl = 'http://localhost:9292'
    }

    serviceProviders {
        // The name of the provider needs to match what the client specified initially. See /consumer/__test__/helpers/pactSetup.js
        ClientsApi {
            // The provider needs to start and stop. "startProvider" and "terminateProvider" are gradle tasks
            startProviderTask = startProvider
            terminateProviderTask = terminateProvider
            requestFilter = { req ->
                // For some reason even when the server is up and running I needed to introduce a bit of delay :(
                Thread.sleep(50)
            }
            protocol = 'http'
            host = 'localhost'
            port = 8080
            path = '/'
            
            // Signal that the pacts will come from the pact broker.
            fromPactBroker {}
        }
    }
}
```

In order to publish the results of the verification back to the broker gradle needs to run with the flag `pact.verifier.publishResults` 
set to true. A quick and easy way to do that is to use `System.setProperty("pact.verifier.publishResults", "true");` in your `build.gradle`.
You can also invoke gradle with: `-Ppact.verifier.publishResults=true`.

One of the important aspects to keep in mind, is that the provider needs to execute in order to run the verification task.
In this case, we created the following tasks to manage the provider's lifecycle in the context of the contract verification:

```groovy
task startProvider(type: SpawnProcessTask, dependsOn: 'shadowJar') {
    command "java -jar ${shadowJar.archiveFile.get().asFile.path}"
    ready 'Started Application' // This is important, as it is the string the the plugin looks for in order to proceed
}

task terminateProvider(type: KillProcessTask)
```

The code here should be relatively straightforward as the `startProvider` task runs after the uber jar has been created,
and it just starts that jar. The `terminateProvider` task kills the process running the jar once the verification ends.

This requires the usage of two other gradle plugins:
* [shadow](https://github.com/johnrengelman/shadow) to assemble the uber jar
* [spawn](https://plugins.gradle.org/plugin/com.wiredforcode.spawn) to run the jar as a different process 

This setup works well, but as the service becomes more elaborate it may be needed to switch from running the jar directly
to using something like docker compose. Something like [this plugin](https://github.com/avast/gradle-docker-compose-plugin)
may be an interesting choice.

#### Junit 5
An alternative way to run the pact verification in this context is to use the [junit 5 extension](https://docs.pact.io/implementation_guides/jvm/provider/junit5). It has some advantages
over the gradle task, namely more control over the initial required state for each interaction in the contract, and if you're
already using something like the [test containers](https://www.testcontainers.org/) its a breeze to set up even a provider
with other dependencies (databases, caches, etc).

With the junit integration, the pact verification and publication runs as part of the test suite. So you can run the tests
with the following command:

```shell script
gradlew test
```

__Behind the scenes:__

In order to run the pact verification as part of the test suite, the following dependency is required (in `build.gradle`):

```groovy
implementation 'au.com.dius.pact.provider:junit5:4.1.9'
```

With that out of the way, your test class is almost a regular junit test, with a few nuances:

* The class needs to be annotated with the `@Provider("ClientsApi")` annotation. Replace `ClientsApi` with the name of the
provider that was defined when the consumer generated the contract;
* In this case we're using a broker, so we also need to specify that via annotation on the class: `@PactBroker(host = "localhost", port = "9292", scheme = "http")`;
* Before any test runs we need to ensure that the provider is up and running. In this case we're using the standard junit 5 
`@BeforeAll` method annotation;
* When all the tests are done, the provider should shut down. In this case we're using the standard junit 5 `@AfterAll` method
annotation to do that;
* A test target needs to be defined, using the @TestTarget annotation;
* For each interaction in the contract, a new method with the `@State` annotation is required. This allows the setup of any
required state for a particular interaction.
* Finally, test template needs to be provided, e.g:
```kotlin
@TestTemplate
@ExtendWith(PactVerificationInvocationContextProvider::class)
fun pactVerificationTestTemplate(context: PactVerificationContext) {
    context.verifyInteraction()
}
```

Note that in order to publish the verification result, the `pact.verifier.publishResults` needs to be set.
The full working example can be found at `/provider/src/test/kotlin/com/example/PactVerificationTests.kt`.

This approach is more flexible than using the gradle task, note however that its probably a good idea not to run the pact
verifications as part of the regular test suite (as it will slow down your regular unit tests). A good idea would be to tag
your contract tests, so that you can selectively run only on specific environments. 

For instance, if the contract test is annotated with `@Tag("ContractTest")`, in `build.gradle` we could have:

```groovy
tasks.withType(Test) {
    useJUnitPlatform{
        if (System.getenv("CI") != "true") {
            excludeTags "ContractTest"
        }
    }
}
```

## Contributing

1. Fork it (<https://github.com/yourname/yourproject/fork>)
2. Create your feature branch (`git checkout -b feature/fooBar`)
3. Commit your changes (`git commit -am 'Add some fooBar'`)
4. Push to the branch (`git push origin feature/fooBar`)
5. Create a new Pull Request
