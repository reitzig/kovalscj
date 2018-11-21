package kovalscj

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// TODO consolidate with JSON Pointer; implement properly

inline class JsonPath(val elements: List<String>) {
    override fun toString(): String {
        return elements.joinToString("/")
    }

    fun dropFirst() : JsonPath {
        return JsonPath(elements.drop(1))
    }

    companion object {
        const val ROOT = "/"
        const val ID = "#"
        const val CURRENT = "."
        const val PARENT = ""

        operator fun invoke(path: String) : JsonPath {
            return JsonPath(path.split("/"))
            // TODO: handle absolute paths correctly
        }
    }
}

fun JsonElement.get(path: JsonPath) : JsonElement? {
    return if ( path.elements.isEmpty()) {
        this
    } else if (path.elements.first() == JsonPath.ROOT) {
        get(path.dropFirst())
    } else if (path.elements.first() == JsonPath.CURRENT) {
        get(path.dropFirst())
    } else if (path.elements == listOf(JsonPath.PARENT)) {
        TODO("Not yet implemented!")
    } else {
        when (this) {
            is JsonObject -> {
                this.getOrNull(path.elements.first())?.get(path.dropFirst())
            }
            is JsonArray -> {
                val index = path.elements.first().toIntOrNull()
                if (index != null) {
                    this.getOrNull(index)?.get(path.dropFirst())
                } else {
                    null
                }
            }
            else          ->
                null
        }
    }
}