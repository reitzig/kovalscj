package kovalscj

import koparj.Json

internal interface Parser<S: JsonSchema.Schema> {
    /**
     * @throws InvalidJsonSchema
     */
    fun parse(json: Json.Object<*,*>) : S
}

internal interface ComponentParser<C: JsonSchema.Component> {
    val key: String

    /**
     * @throws InvalidJsonSchema
     */
    fun parse(json: Json.Element<*>, pointer: JsonPointer) : C

    companion object {
        fun parseAsString(json: Json.Element<*>): String {
            when (json) {
                is Json.String -> return json.value
                else -> throw InvalidJsonSchema("Can not parse string from: '$json'")
            }
        }

        fun parseAsObject(json: Json.Element<*>): Json.Object<*,*> {
            when (json) {
                is Json.Object<*,*> -> return json
                else -> throw InvalidJsonSchema("Can not parse object from: '$json'")
            }
        }

        fun parseAsArray(json: Json.Element<*>): Json.Array<*> {
            when (json) {
                is Json.Array<*> -> return json
                else -> throw InvalidJsonSchema("Can not parse array from: '$json'")
            }
        }
    }
}
