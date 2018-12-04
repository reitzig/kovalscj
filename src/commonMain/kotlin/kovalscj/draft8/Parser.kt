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
        val parser = componentParsers[key]
        check(parser == null || parser.key == key)

        // TODO: if it's null, log warning "Unknown key in schema: '$key'"
        return parser
    }

    private val componentParsers = listOf<ComponentParser<*>>(
        Annotation.Definitions,
        Annotation.Description,
        Annotation.Id,
        Annotation.Title,

        Assertion.Const,
        Assertion.Enum,
        Assertion.Properties,
        Assertion.Type
    ).map { Pair(it.key, it)  }.toMap()
}