package com.pacoca.screenmatch.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Converter}.
 *
 * <h2>What we test</h2>
 * <ul>
 *     <li><b>dataConverter(String, Class&lt;T&gt;)</b>
 *         <ul>
 *             <li>Successfully converts a JSON string into a Java object of the requested type.</li>
 *             <li>Wraps parsing errors into a {@link RuntimeException}.</li>
 *         </ul>
 *     </li>
 *     <li><b>convertFromChatGPT(String)</b>
 *         <ul>
 *             <li>Reads the first choice from the JSON tree: <code>choices[0].message.content</code>.</li>
 *             <li>Returns that content with an extra <code>" ."</code> appended.</li>
 *             <li>Returns the default message <code>"I don't know what to say, sorry. "</code> when:
 *                 <ul>
 *                     <li><code>choices</code> is missing or not an array, or</li>
 *                     <li><code>choices</code> is an empty array.</li>
 *                 </ul>
 *             </li>
 *             <li>If <code>content</code> is missing, it returns the default text + <code>" ."</code>.</li>
 *         </ul>
 *     </li>
 * </ul>
 */
class ConverterTest {

    /**
     * Simple POJO used as a target type for {@link Converter#dataConverter(String, Class)}.
     * Jackson can map JSON fields to these public fields automatically.
     */
    public static class DummyShow {
        public String title;
        public int year;
    }

    // -------------------------------------------------------------------------
    // Tests for dataConverter(String, Class<T>)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("dataConverter should map valid JSON into the target class")
    void dataConverter_shouldMapValidJsonIntoTargetClass() {
        // Arrange
        Converter converter = new Converter();
        String json = """
                {
                  "title": "Breaking Bad",
                  "year": 2008
                }
                """;

        // Act
        DummyShow result = converter.dataConverter(json, DummyShow.class);

        // Assert
        assertNotNull(result, "Result object should not be null");
        assertEquals("Breaking Bad", result.title);
        assertEquals(2008, result.year);
    }

    @Test
    @DisplayName("dataConverter should throw RuntimeException when JSON is invalid")
    void dataConverter_shouldThrowRuntimeException_onInvalidJson() {
        // Arrange
        Converter converter = new Converter();
        String invalidJson = "{ invalid json";

        // Act + Assert
        assertThrows(RuntimeException.class,
                () -> converter.dataConverter(invalidJson, DummyShow.class),
                "Invalid JSON should cause dataConverter to throw RuntimeException");
    }

    // -------------------------------------------------------------------------
    // Tests for convertFromChatGPT(String)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("convertFromChatGPT should return first choice content with extra ' .' at the end")
    void convertFromChatGPT_shouldReturnContentWithTrailingDot() {
        // Arrange: JSON shaped like OpenAI chat completion response
        String json = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "You should watch Better Call Saul"
                      }
                    }
                  ]
                }
                """;

        // Act
        String result = Converter.convertFromChatGPT(json);

        // Assert
        assertEquals("You should watch Better Call Saul .", result);
    }

    @Test
    @DisplayName("convertFromChatGPT should return default message when choices array is empty")
    void convertFromChatGPT_shouldReturnDefault_whenChoicesArrayIsEmpty() {
        // Arrange
        String json = """
                {
                  "choices": []
                }
                """;

        // Act
        String result = Converter.convertFromChatGPT(json);

        // Assert
        assertEquals("I don't know what to say, sorry. ", result,
                "Empty choices array should cause default message to be returned");
    }

    @Test
    @DisplayName("convertFromChatGPT should return default message when choices field is missing")
    void convertFromChatGPT_shouldReturnDefault_whenChoicesFieldMissing() {
        // Arrange: no "choices" field at all
        String json = """
                {
                  "id": "abc123",
                  "object": "chat.completion"
                }
                """;

        // Act
        String result = Converter.convertFromChatGPT(json);

        // Assert
        assertEquals("I don't know what to say, sorry. ", result,
                "Missing choices field should cause default message to be returned");
    }

    @Test
    @DisplayName("convertFromChatGPT should fall back to default content when message.content is missing")
    void convertFromChatGPT_shouldReturnDefaultContent_whenMessageContentMissing() {
        // Arrange: choices exists, but message.content is missing
        String json = """
                {
                  "choices": [
                    {
                      "message": {
                      }
                    }
                  ]
                }
                """;

        // Act
        String result = Converter.convertFromChatGPT(json);

        // Assert
        // Note: convertFromChatGPT uses asText("default") + " ."
        // so we expect the default plus extra " ."
        assertEquals("I don't know what to say, sorry.  .", result,
                "Missing content should cause default text plus ' .' to be returned");
    }
}
