package kovalscj

import kotlinx.serialization.json.JsonElement
import kovalscj.JsonSchema.Component

sealed class Schema : Validating {
    data class Proper(val components: List<Component>) : Schema() {
        override fun validate(json: JsonElement): ValidationResult {
            return components.
                filter { it is Validating }.
                map { (it as Validating).validate(json) }.
                reduce(ValidationResult::plus)
            // TODO: sort so that type (and ... ?) is checked first
            // TODO: migrate to coroutines or parallel collection
        }
    }

    data class Reference(val uri: String) : Schema() {
        override fun validate(json: JsonElement): ValidationResult {
            TODO("not implemented")
            // look up schema (--> path in this schema, or from cache), then validate
        }
    }

    object True : Schema() {
        override fun validate(json: JsonElement): ValidationResult {
            return ValidationResult.Valid(json)
        }
    }

    object False : Schema() {
        override fun validate(json: JsonElement): ValidationResult {
            return ValidationResult.Invalid(ValidationError("foo"))
        }
    }
}