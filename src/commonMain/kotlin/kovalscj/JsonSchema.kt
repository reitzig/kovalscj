package kovalscj

import koparj.Json
import koparj.parser.JsonParser
import kovalscj.ComponentParser.Companion.parseAsString
import kovalscj.JsonMetaSchema.Companion.MetaSchemaPointer
import kovalscj.ValidationOptions.Companion.DEFAULT
import kovalscj.ValidationOptions.Companion.QUIET

// TODO: add paths or root+parent+index (?)
// TODO: add path resolving

class JsonSchema internal constructor (
    internal val jsonMetaSchema: JsonMetaSchema,
    internal val actualSchema: Schema
) {
    fun validate(json: Json.Element<*>, options: ValidationOptions = DEFAULT): ValidationResult {
        return actualSchema.validate(json, options)
    }

    fun validateOrThrow(json: Json.Element<*>, options: ValidationOptions = QUIET) {
        val result = validate(json, options)
        if (!result.valid) {
            throw result.asError()
        }
    }

    fun validate(json: String, options: ValidationOptions = DEFAULT) : ValidationResult {
        return validate(JsonParser.parse(json), options)
    }

    fun validateOrThrow(json: String, options: ValidationOptions = QUIET) {
        validateOrThrow(JsonParser.parse(json), options)
    }

    companion object {
        operator fun invoke(schema: URL): JsonSchema {
            // TODO: add schema cache
            return this(download(schema))
        }

        operator fun invoke(json: String): JsonSchema {
            // TODO: Make more efficient by directly parsing to `JsonSchema`
            //       --> custom `DeserializationStrategy<JsonSchema>`
            return this(JsonParser.parse(json))
        }

        operator fun invoke(json: Json.Element<*>): JsonSchema {
            if (json !is Json.Object<*,*>) {
                throw InvalidJsonSchema("Schemas have to be JSON objects.")
            }
            val metaSchemaUrl = (json[MetaSchemaPointer] as? Json.String)?.value ?:
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

            fun parseFromJson(value: Json.Element<*>) : DataType =
                    parseFromString(parseAsString(value))
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

fun Json.Element<*>.isValid(schema: JsonSchema, options: ValidationOptions = QUIET) : Boolean {
    return schema.validate(this, options).valid
}

fun Json.Element<*>.isValid(options: ValidationOptions = QUIET) : Boolean {
    return this.validate(options).valid
}

fun Json.Element<*>.validate(schema: JsonSchema, options: ValidationOptions = DEFAULT) : ValidationResult {
    return schema.validate(this, options)
}

fun Json.Element<*>.validate(options: ValidationOptions = DEFAULT) : ValidationResult {
    TODO("Not yet implemented!")
    // --> extract schema URL from JSON
}
