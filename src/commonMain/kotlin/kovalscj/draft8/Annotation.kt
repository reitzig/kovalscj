package kovalscj.draft8

import koparj.Json
import kovalscj.ComponentParser
import kovalscj.ComponentParser.Companion.parseAsObject
import kovalscj.ComponentParser.Companion.parseAsString
import kovalscj.JsonPointer
import kovalscj.JsonSchema.Component

// TODO adapt for Draft 8, copy-paste doc
// TODO add rest

sealed class Annotation(override val key: String) : Component {
    data class Id(val value: String) : Annotation(key) {
        companion object : ComponentParser<Id> {
            override val key: String = "\$id"

            override fun parse(json: Json.Element<*>, pointer: JsonPointer): Id =
                Id(parseAsString(json))
        }
    }

    data class Title(val value: String) : Annotation(key) {
        companion object : ComponentParser<Title> {
            override val key: String = "title"

            override fun parse(json: Json.Element<*>, pointer: JsonPointer): Title =
                Title(parseAsString(json))
        }
    }

    data class Description(val value: String) : Annotation(key) {
        companion object : ComponentParser<Description> {
            override val key: String = "description"

            override fun parse(json: Json.Element<*>, pointer: JsonPointer): Description =
                Description(parseAsString(json))
        }
    }

    /* * * * * * * *
     *
     * 9. Schema Re-Use With "definitions"
     *
     * * * * * * * */

    /**
     * The "definitions" keywords provides a standardized location for schema authors to inline re-usable
     * JSON Schemas into a more general schema. The keyword does not directly affect the validation result.
     *
     * This keyword's value MUST be an object. Each member value of this object MUST be a valid JSON Schema.
     */
    data class Definitions(val definitions: Map<String, Schema>) : Annotation(key) {
        constructor(vararg definitions: Pair<String, Schema>) : this(definitions.toMap())

        operator fun get(key: String) =
            definitions[key]

        companion object : ComponentParser<Definitions> {
            override val key: String = "definitions"

            override fun parse(json: Json.Element<*>, pointer: JsonPointer): Definitions =
                   Definitions(parseAsObject(json)
                       .map { it.key.value to Parser.parse(it.value, pointer + it.key.value) }
                       .toMap()
                   )
        }
    }
}
