package de.smartsquare.wecky

import com.amazonaws.services.dynamodbv2.model.StreamRecord
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class JacksonAwsMapperBuilder {
    fun build(): ObjectMapper {
        return jacksonObjectMapper()
                .addMixIn(StreamRecord::class.java, StreamRecordMixin::class.java)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    }

    //little hack needed, since this field is coming along as a float (?!)
    class StreamRecordMixin(
            @JsonIgnore
            val approximateCreationDateTime: Float
    )
}