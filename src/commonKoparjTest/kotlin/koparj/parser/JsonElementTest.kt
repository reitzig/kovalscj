package koparj.parser

import koparj.asJson
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonElementTest {
    @Test
    fun toString_should_equal_plain_print() {
        // Given
        val o = mapOf(
            "foo" to 77,
            "bar" to arrayOf(
                true,
                false,
                arrayOf<Any?>(null)
            )
        ).asJson()

        // When
        val outA = (o as JsonElement).toString(false)
        val outB = (o as JsonElement).toString()

        // Then
        assertEquals(outA, outB)
    }

    @Test
    fun plain_print_should_work() {
        // Given
        val o = mapOf(
            "foo" to 77,
            "bar" to arrayOf(
                true,
                false,
                arrayOf<Any?>(null)
            )
        ).asJson()

        // When
        val out = (o as JsonElement).toString(false)
        println(out)

        // Then
        assertEquals("{ \"foo\" : 77, \"bar\" : [ true, false, [ null ] ] }", out)
    }

    @Test
    fun pretty_print_should_work() {
        // Given
        val o = mapOf(
            "foo" to 77,
            "bar" to arrayOf(
                true,
                false,
                arrayOf<Any?>(null)
            )
        ).asJson()

        // When
        val out = (o as JsonElement).toString(true)
        println(out)

        // Then
        assertEquals("""
            {
                "foo" : 77,
                "bar" : [
                    true,
                    false,
                    [
                        null
                    ]
                ]
            }
            """.trimIndent(),
            out
        )
    }
}
