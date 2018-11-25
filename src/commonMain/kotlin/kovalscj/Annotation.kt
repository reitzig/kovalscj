package kovalscj

import kovalscj.JsonSchema.Component

// TODO adapt for Draft 8, copy-paste doc
// TODO add rest

sealed class Annotation(override val key: String) : Component {
    data class Id(val value: String) : Annotation("\$id")
    data class Title(val value: String) : Annotation("title")
    data class Description(val value: String) : Annotation("description")

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
    data class Definitions(val definitions: Map<String, JsonSchema>) : Annotation("definitions")
}