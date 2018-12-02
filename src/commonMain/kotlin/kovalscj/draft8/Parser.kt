package kovalscj.draft8

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kovalscj.ComponentParser
import kovalscj.InvalidJsonSchema
import kovalscj.JsonMetaSchema.Companion.MetaSchemaPointer
import kovalscj.JsonPointer
import kovalscj.Parser

object Parser : Parser<Schema> {
    override fun parse(json: JsonObject): Schema {
        return parse(json, JsonPointer("#/"))
    }

    /**
     * @throws InvalidJsonSchema
     */
    internal fun parse(json: JsonElement, path: JsonPointer) : Schema {
        when (json) {
            is JsonPrimitive -> {
                return when (json.booleanOrNull) {
                    true -> Schema.True
                    false -> Schema.False
                    else -> throw InvalidJsonSchema("Not a valid schema: '$json'")
                }
            }
            is JsonObject -> {
                return Schema.Proper(
                    json.content.
                        filterNot { it.key == MetaSchemaPointer.elements.last() }.
                        mapNotNull {
                            val key = it.key
                            val value = it.value

                            val component = parserFor(key)?.parse(value, path + key)
                            check(component == null || component.key == key)
                            return@mapNotNull component
                        }
                )
            }
            else -> throw InvalidJsonSchema("Can not parse JSON of type '${json::class.simpleName}' into a JSON schema.")
        }
    }

    /**
     * @throws InvalidJsonSchema
     */
    private fun parserFor(key: String) : ComponentParser<*>? {
        val parser = when (key) {
            Annotation.Id.key -> Annotation.Id
            Annotation.Title.key -> Annotation.Title
            Annotation.Description.key -> Annotation.Description
            Annotation.Definitions.key -> Annotation.Definitions

            Assertion.Type.key -> Assertion.Type
            Assertion.Enum.key -> Assertion.Enum
            else -> null // TODO: log warning "Unknown key in schema: '$key'"
        }
        check(parser == null || parser.key == key)

        return parser
    }
}