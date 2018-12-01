package kovalscj

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// TODO consolidate with JSON Pointer; implement properly
inline class JsonPointer(val elements: List<String>) {
    constructor(path: String) : this(path.split("/"))

    override fun toString(): String {
        return elements.joinToString("/")
    }

    fun dropFirst() : JsonPointer {
        return JsonPointer(elements.drop(1))
    }

    companion object {
        const val ROOT = "#"
        const val CURRENT = "."
        const val PARENT = ".."

        operator fun invoke(path: String) : JsonPointer {
            return JsonPointer(path.split("/"))
            // TODO: handle absolute paths correctly
        }
    }

    operator fun plus(path: String) =
            this.plus(JsonPointer(path))

    operator fun plus(other: JsonPointer) =
            JsonPointer(this.elements + other.elements)
}

operator fun JsonElement.get(pointer: JsonPointer) : JsonElement? {
    return if ( pointer.elements.isEmpty()) {
        this
    } else if (pointer.elements.first() == JsonPointer.ROOT) {
        get(pointer.dropFirst())
    } else if (pointer.elements.first() == JsonPointer.CURRENT) {
        get(pointer.dropFirst())
    } else if (pointer.elements == listOf(JsonPointer.PARENT)) {
        TODO("Not yet implemented!")
    } else {
        when (this) {
            is JsonObject -> {
                this.getOrNull(pointer.elements.first())?.get(pointer.dropFirst())
            }
            is JsonArray -> {
                val index = pointer.elements.first().toIntOrNull()
                if (index != null) {
                    this.getOrNull(index)?.get(pointer.dropFirst())
                } else {
                    null
                }
            }
            else          ->
                null
        }
    }
}