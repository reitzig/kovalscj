package kovalscj

import kovalscj.JsonSchema.Component

// TODO adapt for Draft 8, copy-paste doc
// TODO add rest

sealed class Annotation(override val key: String) : Component {
    data class Id(val value: String) : Annotation("\$id")
    data class Title(val value: String) : Annotation("title")
    data class Description(val value: String) : Annotation("description")

    data class Definitions(val definitions: Map<String, JsonSchema>) : Annotation("definitions")
}