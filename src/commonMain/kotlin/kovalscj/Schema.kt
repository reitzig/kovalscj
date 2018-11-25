package kovalscj

import kotlinx.serialization.json.JsonElement
import kovalscj.JsonSchema.Component
import kovalscj.ValidationResult.Invalid
import kovalscj.ValidationResult.Valid

sealed class Schema : Validating {
    data class Proper(val components: List<Component>) : Schema() {
        override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult {
            return components.
                filter { it is Validating }.
                map { (it as Validating).validate(json, options) }.
                reduce(ValidationResult::plus)
            // TODO: sort so that type (and ... ?) is checked first
            // TODO: migrate to coroutines or parallel collection
        }
    }

    object True : Schema() {
        override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult =
                Valid(options)
    }

    object False : Schema() {
        override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult =
                Invalid(
                    ValidationMessage(
                        "Value should not exist.",
                        JsonPath(listOf()), // TODO implement
                        JsonPath(listOf()) // TODO implement
                    ),
                    options
                )
    }
}