package kovalscj

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTreeParser
import kovalscj.ValidationResult.Invalid
import kovalscj.ValidationResult.Valid

expect class URL
expect fun download(from: URL): String

// TODO: add configuration for format support

interface Validating {
    fun validate(json: JsonElement) : ValidationResult

    fun validateOrThrow(json: JsonElement) {
        when (val result = validate(json)) {
            is Valid -> return
            is Invalid -> throw result.asError()
        }
    }

    fun validate(json: String) : ValidationResult {
        return validate(JsonTreeParser.parse(json))
    }

    fun validateOrThrow(json: String) {
        validateOrThrow(JsonTreeParser.parse(json))
    }
}

// TODO: add support for (aggregatable) json & schema coverage
//  - which parts of the JSON were touched by an assertion?
//  - which assertions where hit, with which result?

sealed class ValidationResult {
    abstract infix operator fun plus(other: ValidationResult) : ValidationResult

    data class Valid(val parsed: JsonElement) : ValidationResult() {
        override fun plus(other: ValidationResult) : ValidationResult {
            return when (other) {
                is Valid -> this
                is Invalid -> other
            }
        }
    }

    data class Invalid internal constructor(val errors: List<ValidationError>) : ValidationResult() {
        internal constructor(vararg errors: ValidationError) : this(errors.asList())

        override fun plus(other: ValidationResult): ValidationResult {
            return when (other) {
                is Valid -> this
                is Invalid -> Invalid(this.errors + other.errors)
            }
        }

        internal fun asError() : Exception {
            return ValidationError(
                    "Not valid",
                    errors
            )
        }
    }
}

data class ValidationError(
    override val message: String, // TODO add other helpful stuff
    val causes: List<ValidationError> = listOf()
) : Exception(message)

inline class JsonPath(val elements: List<String>) {
    override fun toString(): String {
        return elements.joinToString("/")
    }

    fun dropFirst() : JsonPath {
        return JsonPath(elements.drop(1))
    }

    companion object {
        const val ROOT = "/"
        const val CURRENT = "."
        const val PARENT = ".."

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
            is JsonArray  -> {
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





