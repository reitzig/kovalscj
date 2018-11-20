package kovalscj

import kotlinx.serialization.json.JsonElement
import kovalscj.JsonSchema.Component
import kovalscj.JsonSchema.DataType
import kovalscj.ValidationResult.Invalid
import kovalscj.ValidationResult.Valid

// TODO improve error messages
// TODO add paths
// TODO add coverage
// TODO adapt for Draft 8, copy-paste doc
// TODO add rest

sealed class Assertion(override val key: String) : Component, Validating {

    /* * * * * * * *
     *
     * 6.1. Validation Keywords for Any Instance Type
     *
     * * * * * * * */


    /**
     * The value of this keyword MUST be either a string or an array. If it is an array, elements of the array MUST be strings and MUST be unique.
     *
     * String values MUST be one of the six primitive types ("null", "boolean", "object", "array", "number", or "string"), or "integer" which matches any number with a zero fractional part.
     * An instance validates if and only if the instance is in any of the sets listed for this keyword.
     */
    data class Type(val type: DataType) : Assertion("type") {
        override fun validate(json: JsonElement): ValidationResult {
            return if (json.`is`(type)) {
                Valid(json)
            } else {
                Invalid(ValidationError("Value is not one of type $type"))
            }
        }
    }

    /**
     * The value of this keyword MUST be an array. This array SHOULD have at least one element. Elements in the array SHOULD be unique.
     *
     * An instance validates successfully against this keyword if its value is equal to one of the elements in this keyword's array value.
     *
     * Elements in the array might be of any value, including null.
     */
    data class Enum(val values: List<JsonElement>) : Assertion("enum") {
        override fun validate(json: JsonElement): ValidationResult {
            return if (values.contains(json)) {
                Valid(json)
            } else {
                Invalid(ValidationError("Value is not one of [${values.joinToString(", ")}]"))
            }
        }
    }

    /**
     * The value of this keyword MAY be of any type, including null.
     *
     * An instance validates successfully against this keyword if its value is equal to the value of the keyword.
     */
    data class Const(val value: JsonElement) : Assertion("const") {
        override fun validate(json: JsonElement): ValidationResult {
            return if (value == json) {
                Valid(json)
            } else {
                Invalid(ValidationError("Value is not $value"))
            }
        }
    }

    /* * * * * * * *
     *
     * 6.7. Keywords for Applying Subschemas With Boolean Logic
     *
     * * * * * * * */

    /**
     * This keyword's value MUST be a non-empty array. Each item of the array MUST be a valid JSON Schema.
     *
     * An instance validates successfully against this keyword if it validates successfully against all schemas defined by this keyword's value.
     */
    data class AllOf(val subSchemas : List<JsonSchema>) : Assertion("allOf") {
        init {
            require(subSchemas.isNotEmpty())
        }

        override fun validate(json: JsonElement): ValidationResult {
            // TODO add this-level error
            return subSchemas.map { it.validate(json) }.reduce(ValidationResult::plus)
        }
    }

    /**
     * This keyword's value MUST be a non-empty array. Each item of the array MUST be a valid JSON Schema.
     *
     * An instance validates successfully against this keyword if it validates successfully against at least one schema defined by this keyword's value.
     */
    data class AnyOf(val subSchemas: List<JsonSchema>) : Assertion("anyOf") {
        init {
            require(subSchemas.isNotEmpty())
        }

        override fun validate(json: JsonElement): ValidationResult {
            val results = subSchemas.map { it.validate(json) }

            return if (results.any { it is Valid }) {
                Valid(json)
            } else {
                // TODO add this-level error
                results.reduce(ValidationResult::plus)
            }
        }
    }

    /**
     * This keyword's value MUST be a non-empty array. Each item of the array MUST be a valid JSON Schema.
     *
     * An instance validates successfully against this keyword if it validates successfully against exactly one schema defined by this keyword's value.
     */
    data class OneOf(val subSchemas: List<JsonSchema>) : Assertion("oneOf") {
        init {
            require(subSchemas.isNotEmpty())
        }

        override fun validate(json: JsonElement): ValidationResult {
            val results = subSchemas.map { it.validate(json) }

            return if (results.count { it is Valid } == 1) {
                Valid(json)
            } else {
                // TODO add this-level error
                results.reduce(ValidationResult::plus)
            }
        }
    }

    /**
     * This keyword's value MUST be a valid JSON Schema.
     *
     * An instance is valid against this keyword if it fails to validate successfully against the schema defined by this keyword.
     */
    data class Not(val schema: JsonSchema) : Assertion("not") {
        override fun validate(json: JsonElement): ValidationResult {
            return when (schema.validate(json)) {
                is Valid -> Invalid(ValidationError("Validated against $schema"))
                is Invalid -> Valid(json)
            }
        }
    }
}