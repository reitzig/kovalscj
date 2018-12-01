package kovalscj.draft8

import kovalscj.JsonSchema.Component
import kovalscj.Validating

// TODO implement Draft 8 annotators

sealed class Annotator(override val key: String) : Component, Validating {

}