package kovalscj

import kotlinx.serialization.Decoder
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialDescriptor

internal object JsonSchemaDeserializer : DeserializationStrategy<JsonSchema> {
    override val descriptor: SerialDescriptor
        get() = TODO("not yet implemented")

    override fun deserialize(input: Decoder): JsonSchema {
        TODO("not yet implemented")
        // Decide: if this is too nasty, parse into JsonElement and go from there, for now
    }

    override fun patch(input: Decoder, old: JsonSchema): JsonSchema {
        TODO("not yet implemented")
    }
}