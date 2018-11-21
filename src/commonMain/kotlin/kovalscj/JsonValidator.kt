package kovalscj

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTreeParser
import kovalscj.ValidationResult.Invalid
import kovalscj.ValidationResult.Valid

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






