package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedMatrixTest {

    // --- Data Helpers ---
    private final double[][] data3x3 = {
        {1.0, 2.0, 3.0},
        {4.0, 5.0, 6.0},
        {7.0, 8.0, 9.0}
    };

    private final double[][] data2x3 = {
        {10.0, 20.0, 30.0},
        {40.0, 50.0, 60.0}
    };

    // --- TEST 1: Construction & Basic Reads ---
    @Test
    void testConstructorAndReadRowMajor() {
        SharedMatrix matrix = new SharedMatrix(data3x3);

        // Verify Orientation
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation(), "Default constructor should be ROW_MAJOR");
        
        // Verify Data Integrity
        double[][] result = matrix.readRowMajor();
        assertArrayEquals(data3x3, result, "Read data should match constructor data");
    }

    // --- TEST 2: Column Major Loading ---
    @Test
    void testLoadColumnMajor() {
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadColumnMajor(data2x3);

        // Verify Orientation
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation(), "Orientation should be COLUMN_MAJOR");
        
        // Verify Internal Structure (Length should be number of columns, not rows)
        assertEquals(3, matrix.length(), "Column major matrix length should equal number of columns (3)");

        // Verify Data Reconstruction (readRowMajor should transpose it back)
        double[][] result = matrix.readRowMajor();
        assertArrayEquals(data2x3, result, "readRowMajor should return original structure even if stored as Column Major");
    }

    // --- TEST 3: Row Major Switching ---
    @Test
    void testLoadRowMajor() {
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadRowMajor(data2x3);

        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
        assertEquals(2, matrix.length(), "Row major matrix length should equal number of rows (2)");
        assertArrayEquals(data2x3, matrix.readRowMajor());
    }

    // --- TEST 4: Invalid Inputs (Validation) ---
    @Test
    void testInvalidInputs() {
        SharedMatrix matrix = new SharedMatrix();

        // Null
        assertThrows(IllegalArgumentException.class, () -> new SharedMatrix(null));
        assertThrows(IllegalArgumentException.class, () -> matrix.loadRowMajor(null));

        // Empty
        assertThrows(IllegalArgumentException.class, () -> matrix.loadRowMajor(new double[][]{}));
        
        // Jagged Array (Inconsistent row lengths)
        double[][] jagged = {
            {1, 2},
            {3, 4, 5} 
        };
        assertThrows(IllegalArgumentException.class, () -> matrix.loadRowMajor(jagged));
    }

    // --- TEST 5: Individual Vector Access ---
    @Test
    void testGetVector() {
        SharedMatrix matrix = new SharedMatrix(data3x3);
        
        // Get row 1: {4.0, 5.0, 6.0}
        SharedVector vec = matrix.get(1);
        
        assertNotNull(vec);
        assertEquals(3, vec.length());
        assertEquals(4.0, vec.get(0));
        assertEquals(6.0, vec.get(2));
    }

    // --- TEST 6: Empty Matrix Read ---
    @Test
    void testEmptyRead() {
        SharedMatrix matrix = new SharedMatrix(); // Empty constructor
        double[][] result = matrix.readRowMajor();
        assertEquals(0, result.length, "Reading an uninitialized matrix should return empty array");
    }
}