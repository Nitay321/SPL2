package spl.lae;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    ObjectMapper mapper = new ObjectMapper();
    String threads = "4";

    //helper to run main and check output
    void verify(String input, String expected, Path temp) throws IOException {
        File inFile = new File(input);
        File expFile = new File(expected);
        
        assertTrue(inFile.exists());
        assertTrue(expFile.exists());

        File outFile = temp.resolve("out.json").toFile();
        
        // run main
        String[] args = { threads, inFile.getPath(), outFile.getPath() };
        Main.main(args);

        assertTrue(outFile.exists());

        JsonNode real = mapper.readTree(outFile);
        JsonNode exp = mapper.readTree(expFile);

        assertEquals(exp, real);
    }

    @Test
    void test1(@TempDir Path temp) throws IOException {
        verify("examples/example1.json", "examples/out1.json", temp);
    }

    @Test
    void test2(@TempDir Path temp) throws IOException {
        verify("examples/example2.json", "examples/out2.json", temp);
    }

    @Test
    void test3(@TempDir Path temp) throws IOException {
        verify("examples/example3.json", "examples/out3.json", temp);
    }

    @Test
    void test4(@TempDir Path temp) throws IOException {
        verify("examples/example4.json", "examples/out4.json", temp);
    }

    @Test
    void test5(@TempDir Path temp) throws IOException {
        verify("examples/example5.json", "examples/out5.json", temp);
    }

    @Test
    void test6(@TempDir Path temp) throws IOException {
        verify("examples/example6.json", "examples/out6.json", temp);
    }

    @Test
    void testError(@TempDir Path temp) throws IOException {
        // bad json with T operator and 2 operands
        String bad = "{\"operator\": \"T\", \"operands\": [ [[1, 2]], [[3, 4]] ]}";

        File in = temp.resolve("bad.json").toFile();
        File out = temp.resolve("err.json").toFile();
        
        Files.writeString(in.toPath(), bad);

        String[] args = { "4", in.getPath(), out.getPath() };
        Main.main(args);

        assertTrue(out.exists());
        
        JsonNode root = mapper.readTree(out);
        
        // should have error, not result
        assertTrue(root.has("error"));
        assertFalse(root.has("result"));
        
        String msg = root.get("error").asText();
        assertTrue(msg.contains("exactly 1 operand"));
    }
}