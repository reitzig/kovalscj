package koparj.parser

import koparj.Json

sealed class JsonElement : Json.Element<JsonElement> {
    fun toString(pretty: Boolean): kotlin.String =
        // TODO: think interface over. Max depth for pretty? Max depth overall?
        if (pretty) {
            toPrettyString()
        } else {
            toPrettyString(breakLines = false, indent = 0)
        }

    // TODO: make public with well-considered interface?
    internal abstract fun toPrettyString(
        depth: Int = 0,
        breakLines: Boolean = true,
        indent: Int = 4
    ): kotlin.String

    internal data class Object(val members: Map<String, JsonElement>) : JsonElement(),
        Json.Object<String, JsonElement>,
        Map<String, JsonElement> by members {
        constructor(members: Collection<Pair<String, JsonElement>>) : this(members.toMap())
        constructor(vararg members: Pair<String, JsonElement>) : this(mapOf(*members))

        override fun get(key: kotlin.String) : JsonElement? =
            members.get(String(key))
        override fun getValue(key: kotlin.String) : JsonElement =
            members.getValue(String(key))
        override fun getOrElse(key: kotlin.String, defaultValue: () -> JsonElement) : JsonElement =
            members.getOrElse(String(key), defaultValue)

        override fun toString(): kotlin.String = toString(false)

        override fun toPrettyString(depth: Int, breakLines: Boolean, indent: Int): kotlin.String {
            val elementIndent = " ".repeat((depth + 1) * indent)
            return members.entries.joinToString(
                prefix = if (breakLines) "{\n$elementIndent" else "{ ",
                separator = if (breakLines) ",\n$elementIndent" else ", ",
                postfix = if (breakLines) "\n${" ".repeat(depth * indent)}}" else " }"
            ) {
                (it.key as JsonElement).toPrettyString(0, breakLines, indent) +
                    " : " +
                    it.value.toPrettyString(depth + 1, breakLines, indent)
            }
        }
    }

    internal data class Array(val members: List<JsonElement>) : JsonElement(),
        Json.Array<JsonElement>, List<JsonElement> by members {
        constructor(vararg members: JsonElement) : this(listOf(*members))

        override fun toString(): kotlin.String = toString(false)

        override fun toPrettyString(depth: Int, breakLines: Boolean, indent: Int): kotlin.String {
            val elementIndent = " ".repeat((depth + 1) * indent)
            return members.joinToString(
                prefix = if (breakLines) "[\n$elementIndent" else "[ ",
                separator = if (breakLines) ",\n$elementIndent" else ", ",
                postfix = if (breakLines) "\n${" ".repeat(depth * indent)}]" else " ]"
            ) { it.toPrettyString(depth + 1, breakLines, indent) }
        }
    }

    internal data class String(override val value: kotlin.String) : JsonElement(),
        Json.String<JsonElement> {
        override fun toString(): kotlin.String = toString(false)
        override fun toPrettyString(depth: Int, breakLines: Boolean, indent: Int): kotlin.String = "\"${value}\""
    }

    internal data class Number(override val value: kotlin.Number) : JsonElement(),
        Json.Number<JsonElement> {
        override fun toString(): kotlin.String = toString(false)
        override fun toPrettyString(depth: Int, breakLines: Boolean, indent: Int): kotlin.String = "$value"
    }

    internal object True : JsonElement(),
        Json.True<JsonElement> {
        override fun toString(): kotlin.String = toString(false)
        override fun toPrettyString(depth: Int, breakLines: Boolean, indent: Int): kotlin.String = "true"
    }

    internal object False : JsonElement(),
        Json.False<JsonElement> {
        override fun toString(): kotlin.String = toString(false)
        override fun toPrettyString(depth: Int, breakLines: Boolean, indent: Int): kotlin.String = "false"
    }

    internal object Null : JsonElement(),
        Json.Null<JsonElement> {
        override fun toString(): kotlin.String = toString(false)
        override fun toPrettyString(depth: Int, breakLines: Boolean, indent: Int): kotlin.String = "null"
    }
}
