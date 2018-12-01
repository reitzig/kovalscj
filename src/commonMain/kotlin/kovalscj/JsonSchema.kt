package kovalscj

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTreeParser
import kovalscj.JsonMetaSchema.Companion.MetaSchemaPointer
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
        operator fun invoke(schema: URL): JsonSchema {
            // TODO: add schema cache
            return this(download(schema))
        }

        operator fun invoke(json: String): JsonSchema {
            // TODO: Make more efficient by directly parsing to `JsonSchema`
            //       --> custom `DeserializationStrategy<JsonSchema>`
            return this(JsonTreeParser.parse(json))
        }

        operator fun invoke(json: JsonObject): JsonSchema {
            val metaSchemaUrl = (json[MetaSchemaPointer] as? JsonPrimitive)?.content ?:
                throw InvalidJsonSchema("No meta schema found.")

            val metaSchema = JsonMetaSchema(metaSchemaUrl) ?:
                throw InvalidJsonSchema("Doesn't support meta schema '$metaSchemaUrl'")

            val schema = metaSchema.parser.parse(json)

            return JsonSchema(metaSchema, schema)
        }
    }

    // TODO: Figure out where those types belong

    interface Component {
        val key: String
    }

    internal interface Schema : Validating

    enum class DataType {
        Null, Boolean, Array, Object, Number, String, Integer;

        companion object {
            fun parseFromString(value: kotlin.String) : DataType =
                    DataType.values().find { it.name.toLowerCase() == value.toLowerCase() } ?:
                        throw InvalidJsonSchema("Can not parse data type from: '$value'")
        }
    }
}

internal enum class JsonMetaSchema(val url: URL) {
    Draft8(URL("http://json-schema.org/draft-08/schema#")) {
        override val parser: Parser<*>
            get() = kovalscj.draft8.Parser
    };

    abstract val parser: Parser<*>

    companion object {
        val MetaSchemaPointer = JsonPointer("#/\$schema")

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