package koparj

import koparj.parser.JsonElement

fun <E: Any?> Map<String, E>.asJson() : Json.Object<*, JsonElement> =
    JsonElement.Object(this.map { Pair(JsonElement.String(it.key), it.value.asJsonElement()) })
fun <E: Any?> List<E>.asJson() : Json.Array<JsonElement> =
    JsonElement.Array(this.map { it.asJsonElement() })
fun <E: Any?> Array<E>.asJson() : Json.Array<JsonElement> =
    JsonElement.Array(this.map { it.asJsonElement() })
fun String.asJson() : Json.String<JsonElement> =
    JsonElement.String(this)
fun Number.asJson() : Json.Number<JsonElement> =
    JsonElement.Number(this)
fun Boolean.asJson() : Json.Element<JsonElement> =
    if (this) JsonElement.True else JsonElement.False

fun jsonOf(vararg elements : Pair<String, Any?>) : Json.Object<*, JsonElement> = mapOf(*elements).asJson()


private fun Any?.asJsonElement() : JsonElement =
    when (this) {
        null -> JsonElement.Null
        is JsonElement -> this
        is Boolean -> this.asJson() as JsonElement
        is Number -> this.asJson() as JsonElement.Number
        is String -> this.asJson() as JsonElement.String
        is Map<*, *> -> this.mapKeys { "${it.key}" }.asJson() as JsonElement.Object
        is List<*> -> this.asJson() as JsonElement.Array
        is Array<*> -> this.asJson() as JsonElement.Array
        else -> throw UnsupportedOperationException("Can not convert value of type ${this::class} into a JSON element.")
    }
