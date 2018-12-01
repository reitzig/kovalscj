package kovalscj

import kotlinx.serialization.json.JsonElement
import kovalscj.ValidationOption.*
import kovalscj.ValidationOptions.Companion.DEFAULT

// TODO: add configuration for format support

enum class ValidationOption {
    /**
     * Decide in favor of validity in ambiguous/salvageable situations.
     * TODO: document what exactly
     */
    LENIENT,
    /**
     * Conform to the specification exactly.
     *
     * Supersedes [LENIENT].
     */
    STANDARD,
    /**
     * Promote all warnings to errors.
     *
     * Supersedes [LENIENT] and [STANDARD].
     */
    STRICT,

    /**
     * Record coverage of the schema.
     */
    SCHEMA_COVERAGE,
    /**
     * Record coverage of the instance.
     */
    INSTANCE_COVERAGE,

    /**
     * Record errors.
     */
    YIELD_ERRORS,
    /**
     * Record warnings.
     */
    YIELD_WARNINGS
}

inline class ValidationOptions(private val flags : Set<ValidationOption>) {
    constructor(vararg flags: ValidationOption) : this(flags.toSet())

    fun contains(vararg elements: ValidationOption) : Boolean =
        flags.containsAll(elements.toList())
    
    operator fun get(vararg elements: ValidationOption) : Boolean = 
        contains(*elements)

    companion object {
        /**
         * [STANDARD], [YIELD_ERRORS], [YIELD_WARNINGS]
         */
        val DEFAULT = ValidationOptions(STANDARD, YIELD_ERRORS, YIELD_WARNINGS)

        /**
         * [STANDARD]
         */
        val QUIET = ValidationOptions(STANDARD)
    }
}

interface Validating {
    // TODO pass down paths/pointers!
    fun validate(json: JsonElement, options: ValidationOptions) : ValidationResult
}

data class ValidationResult(
    val valid: Boolean,
    val errors: List<ValidationMessage>? = listOf(),
    val warnings: List<ValidationMessage>? = listOf(),
    val schemaCoverage: Coverage? = null,
    val instanceCoverage: Coverage? = null,
    val options: ValidationOptions = DEFAULT
) {
    init {
        // Invariants:
        if (options[YIELD_ERRORS]) {
            require(errors != null)
        }
        if (options[YIELD_WARNINGS]) {
            require(warnings != null)
        }
        if ( options[SCHEMA_COVERAGE]) {
            require(schemaCoverage != null)
            TODO("Not yet implemented!")
        }
        if ( options[INSTANCE_COVERAGE]) {
            require(instanceCoverage != null)
            TODO("Not yet implemented!")
        }

        if (!valid && errors != null) {
            require(errors.isNotEmpty())
        }
    }

    object Valid {
        /**
         * @return An elementary success result.
         */
        operator fun invoke(options: ValidationOptions) : ValidationResult {
            return ValidationResult(
                true,
                if (options[YIELD_ERRORS]) { listOf() } else { null },
                if (options[YIELD_WARNINGS]) { listOf() } else { null },
                if (options[SCHEMA_COVERAGE]) { TODO("Not yet implemented!") } else { null },
                if (options[INSTANCE_COVERAGE]) { TODO("Not yet implemented!") } else { null },
                options
            )
        }
    }

    object Invalid {
        /**
         * @return An elementary error result.
         */
        operator fun invoke(error: ValidationMessage, options: ValidationOptions) : ValidationResult {
            return ValidationResult(
                false,
                if (options[YIELD_ERRORS]) { listOf(error) } else { null },
                if (options[YIELD_WARNINGS]) { listOf() } else { null },
                if (options[SCHEMA_COVERAGE]) { TODO("Not yet implemented!") } else { null },
                if (options[INSTANCE_COVERAGE]) { TODO("Not yet implemented!") } else { null },
                options
            )
        }
    }

    internal fun asError() : Exception {
        require(!valid)

        return InvalidJson(errors!!) // by invariant
    }

    infix operator fun plus(other: ValidationResult) : ValidationResult {
        require(options == other.options) { "Can't combine validation results obtained with different settings." }

        return ValidationResult(
            valid && other.valid,
            // By the invariants, either both are `null` or neither.
            errors?.plus(other.errors!!),
            warnings?.plus(other.warnings!!),
            schemaCoverage?.plus(other.schemaCoverage!!),
            instanceCoverage?.plus(other.instanceCoverage!!),
            options
        )
    }
}

data class InvalidJson(val errors: List<ValidationMessage>) : Exception()
class InvalidJsonSchema(message: String) : Exception(message)
    // TODO May be an `InvalidJson` some day, if we actually validate schema JSON?

data class ValidationMessage(
    override val message: String, // TODO add other helpful stuff
    val inInstance: JsonPointer, // ?
    val inSchema: JsonPointer, // ?
    val causes: List<ValidationMessage> = listOf()
) : Exception(message)

data class Coverage(val m: Map<JsonPointer, List<Pair<JsonPointer, Boolean>>>) {
    fun plus(other: Coverage): Coverage {
        TODO("Not yet implemented!")
    }
}






