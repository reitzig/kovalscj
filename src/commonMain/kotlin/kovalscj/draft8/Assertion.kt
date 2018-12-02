package kovalscj.draft8

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kovalscj.*
import kovalscj.ComponentParser.Companion.parseAsArray
import kovalscj.JsonSchema.Component
import kovalscj.JsonSchema.DataType
import kovalscj.ValidationResult.Invalid
import kovalscj.ValidationResult.Valid

// TODO improve error messages
// TODO add paths
// TODO add coverage
// TODO adapt for Draft 8, copy-paste doc
// TODO add missing assertions

sealed class Assertion(override val key: String) : Component, Validating {

    /* * * * * * * *
     *
     * 6.1. Validation Keywords for Any Instance Type
     *
     * * * * * * * */


    /**
     * The value of this keyword MUST be either a string or an array. If it is an array, elements of the array MUST be
     * strings and MUST be unique.
     *
     * String values MUST be one of the six primitive types ("null", "boolean", "object", "array", "number", or "string"),
     * or "integer" which matches any number with a zero fractional part.
     * An instance validates if and only if the instance is in any of the sets listed for this keyword.
     */
    data class Type(val types: Set<DataType>) : Assertion(key) {
        init {
            require(types.isNotEmpty()) // TODO: confirm that that's in the spirit of the schema?
        }

        override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult {
            return if (types.find { json.`is`(it) } != null ) {
                Valid(options)
            } else {
                Invalid(
                    ValidationMessage(
                        "Value is not one of types $types",
                        JsonPointer(listOf()), // TODO implement
                        JsonPointer(listOf()) // TODO implement
                    ),
                    options
                )
            }
        }

        companion object : ComponentParser<Type> {
            override val key: String = "type"

            override fun parse(json: JsonElement, pointer: JsonPointer): Type {
                return when (json) {
                    is JsonArray -> Type(json.map { DataType.parseFromJson(it) }.toSet())
                        // TODO: protest if array wasn't unique
                    is JsonPrimitive -> Type(setOf(DataType.parseFromJson(json)))
                    else -> throw InvalidJsonSchema("Can not parse a type from: '$json'")
                }
            }
        }
    }

    /**
     * The value of this keyword MUST be an array. This array SHOULD have at least one element. Elements in the array SHOULD be unique.
     *
     * An instance validates successfully against this keyword if its value is equal to one of the elements in
     * this keyword's array value.
     *
     * Elements in the array might be of any value, including null.
     */
    data class Enum(val values: List<JsonElement>) : Assertion(key) {
        override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult {
            return if (values.contains(json)) {
                Valid(options)
            } else {
                Invalid(
                    ValidationMessage(
                        "Value is not one of [${values.joinToString(", ")}]",
                        JsonPointer(listOf()), // TODO implement
                        JsonPointer(listOf()) // TODO implement
                    ),
                    options
                )
            }
        }

        companion object : ComponentParser<Enum> {
            override val key: String = "enum"

            override fun parse(json: JsonElement, pointer: JsonPointer): Enum =
                Enum(parseAsArray(json))
                // TODO: add warnings for empty array and duplicates
        }
    }

    /**
     * The value of this keyword MAY be of any type, including null.
     *
     * An instance validates successfully against this keyword if its value is equal to the value of the keyword.
     */
    data class Const(val value: JsonElement) : Assertion("const") {
        override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult {
            return if (value == json) {
                Valid(options)
            } else {
                Invalid(
                    ValidationMessage(
                        "Value is not $value",
                        JsonPointer(listOf()), // TODO implement
                        JsonPointer(listOf()) // TODO implement
                    ),
                    options
                )
            }
        }
    }

    /* * * * * * * *
    *
    * 6.5. Validation Keywords for Objects
    *
    * * * * * * * */

    /**
     * The value of "properties" MUST be an object. Each value of this object MUST be a valid JSON Schema.
     *
     * This keyword determines how child instances validate for objects, and does not directly validate the
     * immediate instance itself.
     *
     * Validation succeeds if, for each name that appears in both the instance and as a name within this
     * keyword's value, the child instance for that name successfully validates against the corresponding schema.
     *
     * Omitting this keyword has the same behavior as an empty object.
     */
    data class Properties(val propertySchemas: Map<String, Schema>) : Assertion("properties") {
        override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult {
            require(json is JsonObject)

            // TODO: keep a record for additionalProperties
            return json.content.
                mapNotNull { propertySchemas[it.key]?.validate(it.value, options) }.
                reduce(ValidationResult::plus)
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
    * An instance validates successfully against this keyword if it validates successfully against all schemas
    * defined by this keyword's value.
    */
    data class AllOf(val subSchemas : List<Schema>) : Assertion("allOf") {
       init {
           require(subSchemas.isNotEmpty())
       }

       override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult {
           // TODO add this-level error
           return subSchemas.map { it.validate(json, options) }.reduce(ValidationResult::plus)
       }
    }

    /**
    * This keyword's value MUST be a non-empty array. Each item of the array MUST be a valid JSON Schema.
    *
    * An instance validates successfully against this keyword if it validates successfully against at least one
    * schema defined by this keyword's value.
    */
    data class AnyOf(val subSchemas: List<Schema>) : Assertion("anyOf") {
       init {
           require(subSchemas.isNotEmpty())
       }

       override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult {
           val results = subSchemas.map { it.validate(json, options) }

           return if (results.any { it.valid }) {
               results.reduce(ValidationResult::plus).copy(valid = true, errors = listOf())
           } else {
               // TODO add this-level error
               results.reduce(ValidationResult::plus)
           }
       }
    }

    /**
    * This keyword's value MUST be a non-empty array. Each item of the array MUST be a valid JSON Schema.
    *
    * An instance validates successfully against this keyword if it validates successfully against
    * exactly one schema defined by this keyword's value.
    */
    data class OneOf(val subSchemas: List<Schema>) : Assertion("oneOf") {
       init {
           require(subSchemas.isNotEmpty())
       }

       override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult {
           val results = subSchemas.map { it.validate(json, options) }

           return if (results.count { it.valid } == 1) {
               results.reduce(ValidationResult::plus).copy(valid = true, errors = listOf())
           } else {
               // TODO add this-level error
               results.reduce(ValidationResult::plus)
           }
       }
    }

    /**
    * This keyword's value MUST be a valid JSON Schema.
    *
    * An instance is valid against this keyword if it fails to validate successfully against the schema
    * defined by this keyword.
    */
    data class Not(val schema: Schema) : Assertion("not") {
       override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult {
           val result = schema.validate(json, options)

           return if (result.valid) {
               result.copy(
                   valid = false,
                   errors = listOf(
                       ValidationMessage(
                           "Instance validated against forbidden schema.",
                           JsonPointer(listOf()), // TODO implement
                           JsonPointer(listOf()) // TODO implement
                       )
                   )
               )
           } else {
               result.copy(valid = true, errors = listOf())
           }
       }
    }

    /* * * * * * * *
    *
    * 8.3. Schema References With "$ref"
    *
    * * * * * * * */

    /**
    * The "$ref" keyword is used to reference a schema, and provides the ability to validate recursive structures
    * through self-reference.
    *
    * An object schema with a "$ref" property MUST be interpreted as a "$ref" reference. The value of the "$ref"
    * property MUST be a URI Reference. Resolved against the current URI base, it identifies the URI of a schema to use.
    * All other properties in a "$ref" object MUST be ignored.
    *
    * The URI is not a network locator, only an identifier. A schema need not be downloadable from the address if it
    * is a network-addressable URL, and implementations SHOULD NOT assume they should perform a network operation when
    * they encounter a network-addressable URI.
    *
    * A schema MUST NOT be run into an infinite loop against a schema. For example, if two schemas "#alice" and "#bob"
    * both have an "allOf" property that refers to the other, a naive validator might get stuck in an infinite recursive
    * loop trying to validate the instance. Schemas SHOULD NOT make use of infinite recursive nesting like this;
    * the behavior is undefined.
    */
    data class Ref(val url: URL) : Assertion("\$ref") {
       // TODO extend to handle pointers
       // TODO extend to prevent endless recursion
       override fun validate(json: JsonElement, options: ValidationOptions): ValidationResult {
           // TODO resolve while parsing the schema
           return JsonSchema(url).validate(json, options)
       }
    }
}