package kovalscj

import kotlinx.serialization.json.JSON
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// TODO: add paths or root+parent+index (?)
// TODO: add path resolving

class JsonSchema internal constructor (
    val jsonMetaSchema: JsonMetaSchema,
    val actualSchema: Schema
) : Validating {

    override fun validate(json: JsonElement): ValidationResult {
        return actualSchema.validate(json)
    }

    companion object {
        // TODO: add schema cache

        operator fun invoke(schema: URL): JsonSchema {
            return this(download(schema))
        }

        operator fun invoke(json: String): JsonSchema {
            return JSON.parse(JsonSchemaDeserializer, json)
        }

        operator fun invoke(json: JsonObject): JsonSchema {
            TODO("Not yet implemented!")
        }
    }

    interface Component {
        val key: String
    }

    enum class DataType {
        Null, Boolean, Array, Object, Number, String, Integer
    }

}

internal enum class JsonMetaSchema(val url: URL) {
    Draft8(URL("http://json-schema.org/draft-08/schema#"));

    companion object {
        operator fun invoke(url: String) : JsonMetaSchema? {
            return values().find { it.url.toString() == url }
        }
    }
}
