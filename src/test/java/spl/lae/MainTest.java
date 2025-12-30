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
    
    private final String thread_amount= "4"; 

    private void runAndVerify(String inputPath, String expectedOutputPath, Path tempDir) throws IOException {
        File inputFile = new File(inputPath);
        File expectedFile = new File(expectedOutputPath);
        
        assertTrue(inputFile.exists(), "Input file not found: " + inputFile.getAbsolutePath());
        assertTrue(expectedFile.exists(), "Expected output file not found: " + expectedFile.getAbsolutePath());
        File actualFile = tempDir.resolve("actual_output.json").toFile();
        String[] args = { thread_amount, inputFile.getPath(), actualFile.getPath() };
        Main.main(args);
        assertTrue(actualFile.exists(), "Main did not create an output file!");
        JsonNode actualNode = mapper.readTree(actualFile);
        JsonNode expectedNode = mapper.readTree(expectedFile);
        assertEquals(expectedNode, actualNode, "The JSON output for " + inputPath + " did not match the expected structure/values.");
    }


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
        String invalidJson = "{\n" +
                "  \"operator\": \"T\",\n" +
                "  \"operands\": [\n" +
                "    [[1, 2]],\n" +
                "    [[3, 4]]\n" + 
                "  ]\n" +
                "}";

        File inputFile = tempDir.resolve("bad_unary_input.json").toFile();
        File outputFile = tempDir.resolve("unary_error_output.json").toFile();
        java.nio.file.Files.writeString(inputFile.toPath(), invalidJson);

        String[] args = { "4", inputFile.getPath(), outputFile.getPath() };
        Main.main(args);

        assertTrue(outputFile.exists(), "Output file should exist even on error");
        JsonNode outputNode = mapper.readTree(outputFile);
        
        assertTrue(outputNode.has("error"), "Should have 'error' field");
        assertFalse(outputNode.has("result"), "Should NOT have 'result' field");
        
        String errorMessage = outputNode.get("error").asText();
        System.out.println("Caught Expected Parse Error: " + errorMessage);
        assertTrue(errorMessage.contains("requires exactly 1 operand"));
    }


}

