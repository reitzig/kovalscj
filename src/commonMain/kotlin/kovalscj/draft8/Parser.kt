package kovalscj.draft8

import koparj.Json
import kovalscj.ComponentParser
import kovalscj.InvalidJsonSchema
import kovalscj.JsonMetaSchema.Companion.MetaSchemaPointer
import kovalscj.JsonPointer
import kovalscj.Parser

object Parser : Parser<Schema> {
    override fun parse(json: Json.Object<*,*>): Schema {
        return parse(json, JsonPointer("#/"))
    }

    /**
     * @throws InvalidJsonSchema
     */
    internal fun parse(json: Json.Element<*>, path: JsonPointer) : Schema {
        when (json) {
            is Json.True -> return Schema.True
            is Json.False -> return Schema.False
            is Json.Object<*,*> -> {
                return Schema.Proper(
                    json.filterNot { it.key.value == MetaSchemaPointer.elements.last() }.
                        mapNotNull {
                            val keyValue = it.key.value
                            val value = it.value

                            val component = parserFor(keyValue)?.parse(value, path + keyValue)
                            check(component == null || component.key == keyValue)
                            return@mapNotNull component
                        }
                )
            }
            else -> throw InvalidJsonSchema("Can not parse JSON of type '${json::class}' into a JSON schema.")
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
