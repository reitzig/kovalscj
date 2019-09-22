package koparj.parser

import koparj.Json
import kotlin.test.*

class ParserTest {
    // TODO: test objects

    // TODO: test arrays

    // TODO: test strings

    @Test
    fun should_parse_booleans_correctly() {
        assertTrue(JsonParser.parse("true") is Json.True<*>)
        assertTrue(JsonParser.parse("false") is Json.False<*>)
        assertFails { JsonParser.parse("yes") }
        assertFails { JsonParser.parse("no") }
    }

    @Test
    fun should_parse_null_correctly() {
        assertTrue(JsonParser.parse("null") is Json.Null<*>)
        assertFails { JsonParser.parse("nil") }
    }

    @Test
    fun should_parse_integers_correctly() {
        val parsed = JsonParser.parse("77")
        assertTrue(parsed is Json.Number<*>)
        assertEquals(77, parsed.value)
    }

    @Test
    fun should_parse_negative_integers_correctly() {
        val parsed = JsonParser.parse("-42")
        assertTrue(parsed is Json.Number<*>)
        assertEquals(-42, parsed.value)
    }

    @Test
    fun should_parse_floats_correctly() {
        val parsed = JsonParser.parse("77.42")
        assertTrue(parsed is Json.Number<*>)
        assertEquals(77.42, parsed.value)
    }

    @Test
    fun should_parse_negative_floats_correctly() {
        val parsed = JsonParser.parse("-42.77")
        assertTrue(parsed is Json.Number<*>)
        assertEquals(-42.77, parsed.value)
    }

    @Test
    fun should_parse_scientific_notation_correctly() {
        val parsed = JsonParser.parse("-42.77E2")
        assertTrue(parsed is Json.Number<*>)
        assertEquals(-4277.0, parsed.value)
    }

    @Test
    fun should_fail_on_invalid_number_literals() {
        assertFails { JsonParser.parse("1.1.1") }
        assertFails { JsonParser.parse("1.0a2") }
        assertFails { JsonParser.parse(".1") }
        assertFails { JsonParser.parse("01.1") }
        assertFails { JsonParser.parse("1. 1") }
        assertFails { JsonParser.parse("1 .1") }
        assertFails { JsonParser.parse("1 1") }
    }
}
