package parts.code.piggybox.integration.tests.features

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import java.time.Duration
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import parts.code.piggybox.command.CommandServiceApplication
import parts.code.piggybox.integration.tests.TestKafkaConsumer
import parts.code.piggybox.integration.tests.lastRecord
import parts.code.piggybox.kafka.init.KafkaInitServiceApplication
import parts.code.piggybox.preferences.PreferencesServiceApplication
import parts.code.piggybox.schemas.CreatePreferencesCommand
import parts.code.piggybox.schemas.PreferencesCreated
import ratpack.test.MainClassApplicationUnderTest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreatePreferencesFeature {

    private val kafkaInitService = object : MainClassApplicationUnderTest(KafkaInitServiceApplication::class.java) {}
    private val commandService = object : MainClassApplicationUnderTest(CommandServiceApplication::class.java) {}
    private val preferencesService =
        object : MainClassApplicationUnderTest(PreferencesServiceApplication::class.java) {}
    private val consumerPreferencesAuthorization = TestKafkaConsumer.of(CreatePreferencesFeature::class.simpleName)
    private val consumerPreferences = TestKafkaConsumer.of(CreatePreferencesFeature::class.simpleName)

    @BeforeAll
    fun setUp() {
        runBlocking {
            withTimeout(Duration.ofSeconds(30).toMillis()) {
                while (
                    kafkaInitService.address == null ||
                    commandService.address == null ||
                    preferencesService.address == null
                ) {
                    delay(50)
                }
            }
        }

        consumerPreferencesAuthorization.subscribe(listOf("preferences-authorization"))
        consumerPreferences.subscribe(listOf("preferences"))
        consumerPreferencesAuthorization.poll(Duration.ofMillis(1)).count()
        consumerPreferences.poll(Duration.ofMillis(1)).count()
    }

    @AfterAll
    fun tearDown() {
        consumerPreferencesAuthorization.close()
        consumerPreferences.close()
        commandService.close()
        preferencesService.close()
    }

    @Test
    fun `should create preferences`() {
        val customerId = UUID.randomUUID().toString()

        val response = commandService.httpClient.requestSpec { request ->
            request.headers {
                it.set("Content-Type", "application/json")
            }.body.text("""{"customerId": "$customerId", "currency": "EUR"}""")
        }.post("/api/preferences.create")
        response.status.code shouldBe 202

        val command = consumerPreferencesAuthorization.lastRecord(customerId).value() as CreatePreferencesCommand

        UUID.fromString(command.id)
        command.occurredOn shouldNotBe null
        command.customerId shouldBe customerId
        command.currency shouldBe "EUR"

        val event = consumerPreferences.lastRecord(customerId).value() as PreferencesCreated

        UUID.fromString(event.id)
        event.occurredOn shouldNotBe null
        event.customerId shouldBe customerId
        event.currency shouldBe "EUR"
    }
}
