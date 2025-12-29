package spl.lae;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private final ObjectMapper mapper = new ObjectMapper();
    
    // Number of threads to simulate for the tests
    private final String NUM_THREADS = "4"; 

    /**
     * Helper method that:
     * 1. Runs Main.main() with the specific input and a temporary output path.
     * 2. Reads the Actual Output produced by your code.
     * 3. Reads the Expected Output provided by the course staff.
     * 4. Compares them logically (ignoring whitespace/formatting differences).
     */
    private void runAndVerify(String inputPath, String expectedOutputPath, Path tempDir) throws IOException {
        // 1. Setup Paths
        File inputFile = new File(inputPath);
        File expectedFile = new File(expectedOutputPath);
        
        // Fail fast if the example files are missing (e.g. wrong working directory)
        assertTrue(inputFile.exists(), "Input file not found: " + inputFile.getAbsolutePath());
        assertTrue(expectedFile.exists(), "Expected output file not found: " + expectedFile.getAbsolutePath());

        // Create a temporary file for the result of THIS run
        File actualFile = tempDir.resolve("actual_output.json").toFile();

        // 2. Run Main
        // Arguments: [threads, input_path, output_path]
        String[] args = { NUM_THREADS, inputFile.getPath(), actualFile.getPath() };
        Main.main(args);

        // 3. Verify Output Exists
        assertTrue(actualFile.exists(), "Main did not create an output file!");

        // 4. Compare JSON Content
        // We use Jackson to read trees so we compare DATA, not just formatting/whitespace string equality.
        JsonNode actualNode = mapper.readTree(actualFile);
        JsonNode expectedNode = mapper.readTree(expectedFile);

        assertEquals(expectedNode, actualNode, "The JSON output for " + inputPath + " did not match the expected structure/values.");
    }

    // --- Tests for examples/exampleX.json vs examples/outX.json ---

    @Test
    void testExample1(@TempDir Path tempDir) throws IOException {
        runAndVerify("examples/example1.json", "examples/out1.json", tempDir);
    }

    @Test
    void testExample2(@TempDir Path tempDir) throws IOException {
        runAndVerify("examples/example2.json", "examples/out2.json", tempDir);
    }

    @Test
    void testExample3(@TempDir Path tempDir) throws IOException {
        runAndVerify("examples/example3.json", "examples/out3.json", tempDir);
    }

    @Test
    void testExample4(@TempDir Path tempDir) throws IOException {
        runAndVerify("examples/example4.json", "examples/out4.json", tempDir);
    }

    @Test
    void testExample5(@TempDir Path tempDir) throws IOException {
        runAndVerify("examples/example5.json", "examples/out5.json", tempDir);
    }

    @Test
    void testExample6(@TempDir Path tempDir) throws IOException {
        runAndVerify("examples/example6.json", "examples/out6.json", tempDir);
    }
@Test
    void testParseError_InvalidUnaryOperator(@TempDir Path tempDir) throws IOException {
        // Scenario: The 'T' (Transpose) operator must have exactly 1 operand.
        // We provide 2 operands, which should trigger a ParseException.
        String invalidJson = "{\n" +
                "  \"operator\": \"T\",\n" +
                "  \"operands\": [\n" +
                "    [[1, 2]],\n" +
                "    [[3, 4]]\n" + 
                "  ]\n" +
                "}";

        // 1. Setup paths
        File inputFile = tempDir.resolve("bad_unary_input.json").toFile();
        File outputFile = tempDir.resolve("unary_error_output.json").toFile();
        java.nio.file.Files.writeString(inputFile.toPath(), invalidJson);

        // 2. Run Main
        String[] args = { "4", inputFile.getPath(), outputFile.getPath() };
        Main.main(args);

        // 3. Verify Output
        assertTrue(outputFile.exists(), "Output file should exist even on error");
        JsonNode outputNode = mapper.readTree(outputFile);
        
        // Check structure
        assertTrue(outputNode.has("error"), "Should have 'error' field");
        assertFalse(outputNode.has("result"), "Should NOT have 'result' field");
        
        // Verify specific message (Optional but good)
        String errorMessage = outputNode.get("error").asText();
        System.out.println("Caught Expected Parse Error: " + errorMessage);
        assertTrue(errorMessage.contains("requires exactly 1 operand"));
    }


}

