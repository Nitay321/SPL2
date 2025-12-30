package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedMatrixTest {

    private final double[][] data3x3 = {
        {1.0, 2.0, 3.0},
        {4.0, 5.0, 6.0},
        {7.0, 8.0, 9.0}
    };

    private final double[][] data2x3 = {
        {10.0, 20.0, 30.0},
        {40.0, 50.0, 60.0}
    };

    @Test
    void testConstructorAndReadRowMajor() {
        SharedMatrix matrix = new SharedMatrix(data3x3);

        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation(), "Default constructor should be ROW_MAJOR");
        
        double[][] result = matrix.readRowMajor();
        assertArrayEquals(data3x3, result, "Read data should match constructor data");
    }

    @Test
    void testLoadColumnMajor() {
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadColumnMajor(data2x3);

        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation(), "Orientation should be COLUMN_MAJOR");
        
        assertEquals(3, matrix.length(), "Column major matrix length should equal number of columns (3)");

        double[][] result = matrix.readRowMajor();
        assertArrayEquals(data2x3, result, "readRowMajor should return original structure even if stored as Column Major");
    }

    @Test
    void testLoadRowMajor() {
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadRowMajor(data2x3);

        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
        assertEquals(2, matrix.length(), "Row major matrix length should equal number of rows (2)");
        assertArrayEquals(data2x3, matrix.readRowMajor());
    }

    @Test
    void testInvalidInputs() {
        SharedMatrix matrix = new SharedMatrix();

        assertThrows(IllegalArgumentException.class, () -> new SharedMatrix(null));
        assertThrows(IllegalArgumentException.class, () -> matrix.loadRowMajor(null));

        assertThrows(IllegalArgumentException.class, () -> matrix.loadRowMajor(new double[][]{}));
        
        double[][] jagged = {
            {1, 2},
            {3, 4, 5} 
        };
        assertThrows(IllegalArgumentException.class, () -> matrix.loadRowMajor(jagged));
    }

    @Test
    void testGetVector() {
        SharedMatrix matrix = new SharedMatrix(data3x3);
        
        SharedVector vec = matrix.get(1);
        
        assertNotNull(vec);
        assertEquals(3, vec.length());
        assertEquals(4.0, vec.get(0));
        assertEquals(6.0, vec.get(2));
    }

    @Test
    void testEmptyRead() {
        SharedMatrix matrix = new SharedMatrix(); 
        double[][] result = matrix.readRowMajor();
        assertEquals(0, result.length, "Reading an uninitialized matrix should return empty array");
    }
}