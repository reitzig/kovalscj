package kovalscj.draft8

import kotlinx.serialization.json.JsonPrimitive
import kovalscj.InvalidJsonSchema
import kovalscj.JsonMetaSchema
import kovalscj.JsonSchema
import kovalscj.JsonSchema.DataType.Null
import kotlin.test.*

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
        assertEquals(JsonSchema.DataType.Object, type.types.first())
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

    /* * * * * * * * * * * * *
     * ANNOTATIONS
     * * * * * * * * * * * * */

    @Test @Ignore
    fun testBadTitleNumber() {
        val string = """
        {
            "${'$'}schema": "http://json-schema.org/draft-08/schema#",
            "title": 77,
            "description": "Something to test the parser with",
            "type": "object"
        }
        """.trimIndent()

        println(assertFailsWith(InvalidJsonSchema::class) {
            val schema = JsonSchema(string)
        }.message)
    }

    @Test
    fun testBadTitleArray() {
        val string = """
        {
            "${'$'}schema": "http://json-schema.org/draft-08/schema#",
            "title": ["A test schema"],
            "description": "Something to test the parser with",
            "type": "object"
        }
        """.trimIndent()

        println(assertFailsWith(InvalidJsonSchema::class) {
            val schema = JsonSchema(string)
        }.message)
    }

    @Test
    fun testBadDescription() {
        val string = """
        {
            "${'$'}schema": "http://json-schema.org/draft-08/schema#",
            "title": ["A test schema"],
            "description": 77,
            "type": "object"
        }
        """.trimIndent()

        println(assertFailsWith(InvalidJsonSchema::class) {
            val schema = JsonSchema(string)
        }.message)
    }

    @Test
    fun testDefinitions() {
        val string = """
        {
            "${'$'}schema": "http://json-schema.org/draft-08/schema#",
            "title": "A test schema",
            "description": "Something to test the parser with",
            "type": "object",
            "definitions": {
                "foo" : {
                    "type": "string",
                    "enum": ["a", "b", "c"]
                }
            }
        }
        """.trimIndent()

        val schema = JsonSchema(string)

        assertTrue(schema.actualSchema is Schema.Proper)
        assertNotNull(schema.actualSchema["definitions"])
        val definitions = schema.actualSchema["definitions"]
        assertTrue(definitions is Annotation.Definitions)
        val foo = definitions["foo"]
        assertNotNull(foo)
        assertTrue(foo is Schema.Proper)
        assertEquals(2, foo.components.size)
        //assertEquals(JsonPointer("#/definitions/foo"), foo.path) // TODO
    }

    /* * * * * * * * * * * * *
     * ASSERTIONS
     * * * * * * * * * * * * */

    @Test
    fun testSingleType() {
        val string = """
        {
            "${'$'}schema": "http://json-schema.org/draft-08/schema#",
            "title": "A test schema",
            "description": "Something to test the parser with",
            "type": "string"
        }
        """.trimIndent()

        val schema = JsonSchema(string)
        assertTrue(schema.actualSchema is Schema.Proper)

        val type = schema.actualSchema["type"]
        assertTrue(type is Assertion.Type)
        assertEquals(setOf(JsonSchema.DataType.String), type.types)
    }

    @Test
    fun testMultipleTypes() {
        val string = """
        {
            "${'$'}schema": "http://json-schema.org/draft-08/schema#",
            "title": "A test schema",
            "description": "Something to test the parser with",
            "type": ["string", "null"]
        }
        """.trimIndent()

        val schema = JsonSchema(string)
        assertTrue(schema.actualSchema is Schema.Proper)

        val type = schema.actualSchema["type"]
        assertTrue(type is Assertion.Type)
        assertEquals(setOf(JsonSchema.DataType.String, Null), type.types)
    }

    @Test
    fun testBadType() {
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

    @Test
    fun testEnum() {
        val string = """
        {
            "${'$'}schema": "http://json-schema.org/draft-08/schema#",
            "title": "A test schema",
            "description": "Something to test the parser with",
            "type": "integer",
            "enum": [42, 77]
        }
        """.trimIndent()

        val schema = JsonSchema(string)

        assertTrue(schema.actualSchema is Schema.Proper)
        val enum = schema.actualSchema["enum"]
        assertNotNull(enum)
        assertTrue(enum is Assertion.Enum)
        assertEquals(listOf(42, 77), enum.values.mapNotNull { (it as? JsonPrimitive)?.intOrNull })
    }
}

operator fun Schema.Proper.get(key: String): JsonSchema.Component =
    this.components.first { it.key == key }