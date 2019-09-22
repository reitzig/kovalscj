package koparj.parser

import koparj.Json
import kotlin.math.*

object JsonParser: Json<JsonElement> {
    override fun parse(raw: String) : JsonElement {
        return parseValue(raw, PointeredJson()).tree
            ?: throw Exception("invalid") // TODO
    }
}

fun String.parseAsJson() : JsonElement = JsonParser.parse(this)

private class Option<T>(var value: T?)

private class PointeredJson {
    private val root: Option<JsonElement> =
        Option(null)
    private val inserters: MutableList<(JsonElement) -> Unit> =
        mutableListOf<(JsonElement) -> Unit>({ root.value = it })

    val tree: JsonElement?
        get() = root.value

    fun push(inserter: (JsonElement) -> Unit) {
        inserters.add(0, inserter)
    }

    fun pop() {
        inserters.removeAt(0)
    }

    fun insert(newValue: JsonElement) {
        inserters.first().invoke(newValue)
    }
}

private tailrec fun parseValue(input: String, tree: PointeredJson) : PointeredJson {
    // TODO: trim input
    if (input.isBlank()) {
        tree.pop()
        return tree
    }

    val (rest, value) = when (input.first()) {
        '{' -> parseObject(input)
        '[' -> parseArray(input)
        '"' -> parseString(input)
        't' -> parseFixed(input, "true", JsonElement.True)
        'f' -> parseFixed(input, "false", JsonElement.False)
        'n' -> parseFixed(input, "null", JsonElement.Null)
        '-', in CharRange('0', '9') -> parseNumber(input)
        else -> throw Exception("invalid char") // TODO: proper error reporting
    }
    require(rest.length < input.length)

    // TODO: use mutable list of chars --> avoid copying

    tree.insert(value)
    return parseValue(rest, tree)
}

private inline fun parseObject(input: String): Pair<String, JsonElement> {
    TODO("Not yet implemented!")
}

private inline fun parseArray(input: String): Pair<String, JsonElement> {
    TODO("Not yet implemented!")
}

private inline fun parseString(input: String): Pair<String, JsonElement> {
    TODO("Not yet implemented!")
}

private inline fun parseFixed(input: String, pattern: String, result: JsonElement) : Pair<String, JsonElement> {
    if (input.startsWith(pattern)) {
        return Pair(input.drop(pattern.length), result)
    } else {
        throw Exception("Invalid literal: '${input.split("[^\\w\\d]").first()}'")
        // TODO: proper error reporting
    }
}

private inline fun parseNumber(input: String) : Pair<String, JsonElement.Number> {
    val (rest, numberString) = parseNumber(input, "")

    val number: Number = if (numberString.contains('.')) {
        numberString.toDouble()
    } else {
        numberString.toInt()
    }

    return Pair(rest, JsonElement.Number(number))
}

private tailrec fun parseNumber(inputSuffix: String, number: String) : Pair<String, String> {
    if (inputSuffix.isBlank() || !inputSuffix.first().isNumeric()) {
        return Pair(inputSuffix, number)
    }

    // TODO: parse numbers properly
    return parseNumber(inputSuffix.drop(1), number + inputSuffix.first())
}

fun Char.isNumeric() : Boolean =
    when (this) {
        '0' -> true
        '1' -> true
        '2' -> true
        '3' -> true
        '4' -> true
        '5' -> true
        '6' -> true
        '7' -> true
        '8' -> true
        '9' -> true
        else -> false
    }
