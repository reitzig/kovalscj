package kovalscj

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal interface Parser<S: JsonSchema.Schema> {
    /**
     * @throws InvalidJsonSchema
     */
    fun parse(json: JsonObject) : S
}

internal interface ComponentParser<C: JsonSchema.Component> {
    val key: String

    /**
     * @throws InvalidJsonSchema
     */
    fun parse(json: JsonElement, pointer: JsonPointer) : C

    companion object {
        fun parseAsString(json: JsonElement): String {
            when (json) {
                // TODO: Currently, kotlinx.serialization doesn't provide a way to verify it's an actual string literal
                is JsonPrimitive -> {
                    when (val value = json.contentOrNull) {
                        null -> throw InvalidJsonSchema("Can not parse string from: 'null'")
                        else -> return value
                    }
                }
                else -> throw InvalidJsonSchema("Can not parse string from: '$json'")
            }
        }

        fun parseAsObject(json: JsonElement): JsonObject {
            when (json) {
                is JsonObject -> return json
                else -> throw InvalidJsonSchema("Can not parse object from: '$json'")
            }
        }

        fun parseAsArray(json: JsonElement): JsonArray {
            when (json) {
                is JsonArray -> return json
                else -> throw InvalidJsonSchema("Can not parse array from: '$json'")
            }
        }
    }
}