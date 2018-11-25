package kovalscj

import kovalscj.JsonSchema.Component

// TODO implement Draft 8 annotators

sealed class Annotator(override val key: String) : Component, Validating {

}