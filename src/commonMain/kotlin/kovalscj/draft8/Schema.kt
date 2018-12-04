package kovalscj.draft8

import kotlinx.serialization.json.JsonElement
import kovalscj.*
import kovalscj.JsonSchema.Component
import kovalscj.ValidationResult.Invalid
import kovalscj.ValidationResult.Valid

sealed class Schema : Validating, JsonSchema.Schema {
    data class Proper(val components: List<Component>) : Schema() {
        constructor(vararg components: Component) : this(components.toList())

        operator fun get(i: Int) =
            components[i]

        override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult {
            return components.
                filter { it is Validating }.
                map { (it as Validating).validate(json, options) }.
                reduce(ValidationResult::plus)
            // TODO: sort so that type (and ... ?) is checked first
            // TODO: migrate to coroutines or parallel collection
            // TODO: Doesn't work like this with Draft 8 annotators.
            // TODO: warn about duplicates (or error out? what does the spec say?)
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
                        JsonPointer(listOf()), // TODO implement
                        JsonPointer(listOf()) // TODO implement
                    ),
                    options
                )
    }
}