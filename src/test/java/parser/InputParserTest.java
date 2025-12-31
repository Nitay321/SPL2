package parser;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.text.ParseException;
import static org.junit.jupiter.api.Assertions.*;

class InputParserTest {

    InputParser p = new InputParser();

    private String createJson(String data) throws Exception {
        File f = File.createTempFile("test", ".json");
        f.deleteOnExit();
        try (FileWriter w = new FileWriter(f)) {
            w.write(data);
        }
        return f.getAbsolutePath();
    }

    @Test
    void checkBadUnary() throws Exception {
        String s = "{\"operator\": \"T\", \"operands\": [ [[1, 2]], [[3, 4]] ]}";
        String path = createJson(s);

        assertThrows(ParseException.class, () -> p.parse(path));
    }

    @Test
    void checkBadBinary() throws Exception {
        String s = "{\"operator\": \"+\", \"operands\": [ [[1, 2]] ]}";
        String path = createJson(s);

        Exception e = assertThrows(ParseException.class, () -> p.parse(path));
        assertTrue(e.getMessage().contains("at least 2")); 
    }

    @Test
    void checkAssociativity() throws Exception {
        String s = "{\"operator\": \"+\", \"operands\": [ [[1]], [[2]], [[3]] ]}";
        String path = createJson(s);
        
        ComputationNode root = p.parse(path);

        assertNotNull(root);
        assertEquals(ComputationNodeType.ADD, root.getNodeType());
        
        assertEquals(2, root.getChildren().size());
    }
}