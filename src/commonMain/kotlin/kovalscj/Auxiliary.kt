package kovalscj

import kotlinx.serialization.json.*

expect class URL(url: String)
expect fun download(from: URL): String

internal fun JsonElement.`is`(type: JsonSchema.DataType): Boolean {
    return when (type) {
        JsonSchema.DataType.Null    -> this is JsonNull
        JsonSchema.DataType.Boolean -> (this as? JsonLiteral)?.booleanOrNull != null
        JsonSchema.DataType.Number  -> (this as? JsonLiteral)?.doubleOrNull != null
        JsonSchema.DataType.Integer -> (this as? JsonLiteral)?.longOrNull != null
        JsonSchema.DataType.String  -> (this as? JsonLiteral)?.contentOrNull != null
        JsonSchema.DataType.Array   -> this is JsonArray
        JsonSchema.DataType.Object  -> this is JsonObject
    }
}