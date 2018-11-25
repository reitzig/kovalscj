package kovalscj

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTreeParser
import kovalscj.ValidationOption.*
import kovalscj.ValidationOptions.Companion.DEFAULT
import kovalscj.ValidationOptions.Companion.QUIET

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

inline class ValidationOptions(val flags : Set<ValidationOption>) {
    constructor(vararg flags: ValidationOption) : this(flags.toSet())

    fun contains(vararg elements: ValidationOption) =
        flags.containsAll(elements.toList())

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
        if (options.contains(YIELD_ERRORS)) {
            require(errors != null)
        }
        if (options.contains(YIELD_WARNINGS)) {
            require(warnings != null)
        }
        if ( options.contains(SCHEMA_COVERAGE)) {
            require(schemaCoverage != null)
        }
        if ( options.contains(INSTANCE_COVERAGE)) {
            require(instanceCoverage != null)
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
                if (options.contains(ValidationOption.YIELD_ERRORS)) { listOf() } else { null },
                if (options.contains(ValidationOption.YIELD_WARNINGS)) { listOf() } else { null },
                if (options.contains(ValidationOption.SCHEMA_COVERAGE)) { TODO("Not yet implemented!") } else { null },
                if (options.contains(ValidationOption.INSTANCE_COVERAGE)) { TODO("Not yet implemented!") } else { null },
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
                if (options.contains(ValidationOption.YIELD_ERRORS)) { listOf(error) } else { null },
                if (options.contains(ValidationOption.YIELD_WARNINGS)) { listOf() } else { null },
                if (options.contains(ValidationOption.SCHEMA_COVERAGE)) { TODO("Not yet implemented!") } else { null },
                if (options.contains(ValidationOption.INSTANCE_COVERAGE)) { TODO("Not yet implemented!") } else { null },
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

data class ValidationMessage(
    override val message: String, // TODO add other helpful stuff
    val inInstance: JsonPath, // ?
    val inSchema: JsonPath, // ?
    val causes: List<ValidationMessage> = listOf()
) : Exception(message)

data class Coverage(val m: Map<JsonPath, List<Pair<JsonPath, Boolean>>>) {
    fun plus(other: Coverage): Coverage {
        TODO("Not yet implemented!")
    }
}






