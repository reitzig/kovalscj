package kovalscj

import koparj.Json

expect class URL(url: String)
expect fun download(from: URL): String

internal fun Json.Element<*>.`is`(type: JsonSchema.DataType): Boolean {
    return when (type) {
        JsonSchema.DataType.Null    -> this is Json.Null
        JsonSchema.DataType.Boolean -> this is Json.True || this is Json.False
        JsonSchema.DataType.Number  -> this is Json.Number
        JsonSchema.DataType.Integer -> this is Json.Number && (this.value is Long || this.value is Int)
        JsonSchema.DataType.String  -> this is Json.String
        JsonSchema.DataType.Array   -> this is Json.Array
        JsonSchema.DataType.Object  -> this is Json.Object<*,*>
    }
}
