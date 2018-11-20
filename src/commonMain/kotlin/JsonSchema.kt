package kovalscj

import kotlinx.serialization.Decoder
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.json.*
import kovalscj.JsonSchema.*
import kovalscj.ValidationResult.Invalid
import kovalscj.ValidationResult.Valid

// TODO: add paths or root+parent+index (?)
// TODO: add path resolving

sealed class JsonSchema : Validating {

    companion object {
        // TODO: add schema cache

        operator fun invoke(schema: URL): JsonSchema {
            return this(download(schema))
        }

        operator fun invoke(json: String): JsonSchema {
            return JSON.parse(Deserializer, json)
        }

        operator fun invoke(json: JsonObject): JsonSchema {
            TODO("Not yet implemented!")
        }
    }

    interface Component {
        val key: String
    }

    enum class DataType {
        Null, Boolean, Array, Object, Number, String
    }

    internal object Deserializer : DeserializationStrategy<JsonSchema> {
        override val descriptor: SerialDescriptor
            get() = TODO("not yet implemented")

        override fun deserialize(input: Decoder): JsonSchema {
            TODO("not yet implemented")
        }

        override fun patch(input: Decoder, old: JsonSchema): JsonSchema {
            TODO("not yet implemented")
        }
    }

    data class Proper(val components: List<Component>) : JsonSchema() {
        override fun validate(json: JsonElement): ValidationResult {
            return components.
                    filter { it is Validating }.
                    map { (it as Validating).validate(json) }.
                    reduce(ValidationResult::plus)
            // TODO: sort so that type (and ... ?) is checked first
            // TODO: migrate to coroutines or parallel collection
        }
    }

    data class Reference(val uri: String) : JsonSchema() {
        override fun validate(json: JsonElement): ValidationResult {
            TODO("not implemented")
            // look up schema (--> path in this schema, or from cache), then validate
        }
    }

    object True : JsonSchema() {
        override fun validate(json: JsonElement): ValidationResult {
            return Valid(json)
        }
    }

    object False : JsonSchema() {
        override fun validate(json: JsonElement): ValidationResult {
            return Invalid(ValidationError("foo"))
        }
    }
}

fun JsonElement.`is`(type: JsonSchema.DataType): Boolean {
    return when (type) {
        DataType.Null    -> this is JsonNull
        DataType.Boolean -> (this as? JsonLiteral)?.booleanOrNull != null
        DataType.Number  -> (this as? JsonLiteral)?.doubleOrNull != null
        DataType.String  -> (this as? JsonLiteral)?.contentOrNull != null
        DataType.Array   -> this is JsonArray
        DataType.Object  -> this is JsonObject
    }
}