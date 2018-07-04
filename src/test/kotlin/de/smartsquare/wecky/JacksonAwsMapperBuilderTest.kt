package de.smartsquare.wecky

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import org.junit.jupiter.api.Test

internal class JacksonAwsMapperBuilderTest {

    @Test
    fun should_deserialize_dynamodbevent() {
        val mapper = JacksonAwsMapperBuilder().build()
        val stream = javaClass.classLoader.getResourceAsStream("event.json")

        val dyndbEvent = mapper.readValue(stream, DynamodbEvent::class.java)
    }
}