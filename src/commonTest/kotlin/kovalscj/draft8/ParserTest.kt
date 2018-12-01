package kovalscj.draft8

import kovalscj.InvalidJsonSchema
import kovalscj.JsonMetaSchema
import kovalscj.JsonSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ParserTest {
    @Test
    fun miniTest() {
        val string = """
        {
            "${'$'}schema": "http://json-schema.org/draft-08/schema#",
            "title": "A test schema",
            "description": "Something to test the parser with",
            "type": "object"
        }
        """.trimIndent()

        val schema = JsonSchema(string)

        assertEquals(schema.jsonMetaSchema, JsonMetaSchema.Draft8)
        assertTrue(schema.actualSchema is Schema.Proper)
        assertEquals(3, schema.actualSchema.components.size)

        val title = schema.actualSchema["title"]
        assertTrue(title is Annotation.Title)
        assertEquals("A test schema", title.value)

        val description = schema.actualSchema["description"]
        assertTrue(description is Annotation.Description)
        assertEquals("Something to test the parser with", description.value)

        val type = schema.actualSchema["type"]
        assertTrue(type is Assertion.Type)
        assertEquals(JsonSchema.DataType.Object, type.type)
    }

    @Test
    fun testBadMetaSchema() {
        val string = """
        {
            "${'$'}schema": "http://json-schema.org/draft-07/schema#",
            "title": "A test schema",
            "description": "Something to test the parser with",
            "type": "object"
        }
        """.trimIndent()

        println(assertFailsWith(InvalidJsonSchema::class) {
            val schema = JsonSchema(string)
        }.message)
    }

    @Test
    fun testBadTypeSchema() {
        val string = """
        {
            "${'$'}schema": "http://json-schema.org/draft-08/schema#",
            "title": "A test schema",
            "description": "Something to test the parser with",
            "type": "foo"
        }
        """.trimIndent()

        println(assertFailsWith(InvalidJsonSchema::class) {
            val schema = JsonSchema(string)
        }.message)
    }
}

operator fun Schema.Proper.get(key: String): JsonSchema.Component =
    this.components.first { it.key == key }