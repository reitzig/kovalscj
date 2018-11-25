package kovalscj

import kotlinx.serialization.json.JSON
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTreeParser
import kovalscj.ValidationOptions.Companion.DEFAULT
import kovalscj.ValidationOptions.Companion.QUIET

// TODO: add paths or root+parent+index (?)
// TODO: add path resolving

class JsonSchema internal constructor (
    internal val jsonMetaSchema: JsonMetaSchema,
    internal val actualSchema: Schema
) {
    fun validate(json: JsonElement, options: ValidationOptions = DEFAULT): ValidationResult {
        return actualSchema.validate(json, options)
    }

    fun validateOrThrow(json: JsonElement, options: ValidationOptions = QUIET) {
        val result = validate(json, options)
        if (!result.valid) {
            throw result.asError()
        }
    }

    fun validate(json: String, options: ValidationOptions = DEFAULT) : ValidationResult {
        return validate(JsonTreeParser.parse(json), options)
    }

    fun validateOrThrow(json: String, options: ValidationOptions = QUIET) {
        validateOrThrow(JsonTreeParser.parse(json), options)
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

fun JsonElement.isValid(schema: JsonSchema, options: ValidationOptions = QUIET) : Boolean {
    return schema.validate(this, options).valid
}

fun JsonElement.isValid(options: ValidationOptions = QUIET) : Boolean {
    return this.validate(options).valid
}

fun JsonElement.validate(schema: JsonSchema, options: ValidationOptions = DEFAULT) : ValidationResult {
    return schema.validate(this, options)
}

fun JsonElement.validate(options: ValidationOptions = DEFAULT) : ValidationResult {
    TODO("Not yet implemented!")
    // --> extract schema URL from JSON
}