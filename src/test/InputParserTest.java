import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import static org.junit.jupiter.api.Assertions.*;

class InputParserTest {

    private final InputParser parser = new InputParser();

    /**
     * Helper method to create a temporary JSON file for testing.
     * Returns the absolute path of the created file.
     */
    private String createTempJsonFile(String content) throws Exception {
        File tempFile = File.createTempFile("test_input", ".json");
        tempFile.deleteOnExit(); // Clean up after test
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile.getAbsolutePath();
    }

    // --- TEST 1: Check if Unary Operator (T) throws error for 2 operands ---
    @Test
    void testInvalidUnaryOperator() throws Exception {
        // "T" with 2 operands (Should fail)
        String jsonContent = "{" +
                "\"operator\": \"T\"," +
                "\"operands\": [" +
                "   [[1, 2]]," + 
                "   [[3, 4]]" +  // Second operand is illegal!
                "]" +
                "}";

        String path = createTempJsonFile(jsonContent);

        // Expect a ParseException to be thrown
        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse(path);
        });

        // Optional: Check if the message is correct
        System.out.println("Caught Expected Error: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("exactly 1 operand"));
    }

    // --- TEST 2: Check if Binary Operator (+) throws error for 1 operand ---
    @Test
    void testInvalidBinaryOperator() throws Exception {
        // "+" with only 1 operand (Should fail)
        String jsonContent = "{" +
                "\"operator\": \"+\"," +
                "\"operands\": [" +
                "   [[1, 2]]" + // Only 1 operand is illegal!
                "]" +
                "}";

        String path = createTempJsonFile(jsonContent);

        ParseException exception = assertThrows(ParseException.class, () -> {
            parser.parse(path);
        });

        System.out.println("Caught Expected Error: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("at least 2 operands")); // Adjust based on your error message
    }

    // --- TEST 3: Check if Associativity Logic works (A + B + C) ---
    @Test
    void testAssociativityFix() throws Exception {
        // "+" with 3 operands: A, B, C
        // Your logic should convert this to: (A + B) + C
        // This means the ROOT node should strictly have 2 children, not 3.
        String jsonContent = "{" +
                "\"operator\": \"+\"," +
                "\"operands\": [" +
                "   [[1]]," + // A
                "   [[2]]," + // B
                "   [[3]]" +  // C
                "]" +
                "}";

        String path = createTempJsonFile(jsonContent);
        
        // Parse it
        ComputationNode root = parser.parse(path);

        // Assertions
        assertNotNull(root);
        assertEquals(ComputationNodeType.ADD, root.getNodeType());
        
        // Crucial Check: If nesting worked, size should be 2. If it failed, size is 3.
        assertEquals(2, root.getChildren().size(), "Root should have 2 children after nesting (nested node + last operand)");
        
        System.out.println("Associativity Test Passed: 3 operands correctly nested into 2 children.");
    }
}