package memory;
import memory.SharedMatrix;
import memory.SharedVector;
import memory.VectorOrientation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SharedVectorTest {

    private SharedVector rowVector;
    private SharedVector colVector;
    private final double[] data = {1.0, 2.0, 3.0};

    @BeforeEach
    void setUp() {
        // Create fresh vectors before each test
        rowVector = new SharedVector(data.clone(), VectorOrientation.ROW_MAJOR);
        colVector = new SharedVector(data.clone(), VectorOrientation.COLUMN_MAJOR);
    }

    // --- Basic Accessors ---

    @Test
    void testConstructorAndAccessors() {
        assertEquals(3, rowVector.length(), "Length should be 3");
        assertEquals(VectorOrientation.ROW_MAJOR, rowVector.getOrientation());
        assertEquals(1.0, rowVector.get(0));
        assertEquals(3.0, rowVector.get(2));
    }

    // --- Transpose ---

    @Test
    void testTranspose() {
        // Test Row -> Column
        rowVector.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, rowVector.getOrientation());

        // Test Column -> Row
        rowVector.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, rowVector.getOrientation());
    }

    // --- Negate ---

    @Test
    void testNegate() {
        rowVector.negate();
        assertEquals(-1.0, rowVector.get(0));
        assertEquals(-2.0, rowVector.get(1));
        assertEquals(-3.0, rowVector.get(2));
    }

    // --- Addition ---

    @Test
    void testAddSuccess() {
        SharedVector other = new SharedVector(new double[]{4.0, 5.0, 6.0}, VectorOrientation.ROW_MAJOR);
        rowVector.add(other);

        // Expected: {1+4, 2+5, 3+6} -> {5, 7, 9}
        assertEquals(5.0, rowVector.get(0));
        assertEquals(9.0, rowVector.get(2));
    }

    @Test
    void testAddFailDimensions() {
        SharedVector wrongSize = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> rowVector.add(wrongSize), 
                "Should fail when adding vectors of different lengths");
    }

    @Test
    void testAddFailOrientation() {
        assertThrows(IllegalArgumentException.class, () -> rowVector.add(colVector), 
                "Should fail when adding Row vector to Column vector");
    }

    // --- Dot Product ---

    @Test
    void testDotProductSuccess() {
        // Dot product requires: this(ROW) . other(COLUMN)
        // {1,2,3} . {1,2,3} = 1 + 4 + 9 = 14
        double result = rowVector.dot(colVector);
        assertEquals(14.0, result, 0.0001);
    }

    @Test
    void testDotProductFailOrientation() {
        SharedVector row2 = new SharedVector(data.clone(), VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> rowVector.dot(row2), 
                "Dot product should fail if second vector is not COLUMN_MAJOR");
    }

    // --- Vector-Matrix Multiplication (vecMatMul) ---

    @Test
    void testVecMatMulSuccess() {
        // Setup:
        // Vector (1x2): [1, 2]
        // Matrix (2x2):
        // [3, 4]
        // [5, 6]
        //
        // Calculation: [1*3 + 2*5, 1*4 + 2*6] = [13, 16]

        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        
        SharedMatrix m = new SharedMatrix();
        // Matrix must be loaded as COLUMN_MAJOR for vecMatMul to work (per SharedVector logic)
        // Input to loadColumnMajor is usually standard matrix, internally stored as columns.
        double[][] matData = {
            {3.0, 4.0},
            {5.0, 6.0}
        };
        m.loadColumnMajor(matData);

        v.vecMatMul(m);

        assertEquals(2, v.length());
        assertEquals(13.0, v.get(0));
        assertEquals(16.0, v.get(1));
    }

    @Test
    void testVecMatMulFailVectorOrientation() {
        // vecMatMul requires 'this' to be ROW_MAJOR
        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(new double[][]{{1},{2}});
        
        assertThrows(IllegalArgumentException.class, () -> colVector.vecMatMul(m),
            "Should throw if vector is not ROW_MAJOR");
    }

    @Test
    void testVecMatMulFailMatrixOrientation() {
        SharedVector v = new SharedVector(new double[]{1.0}, VectorOrientation.ROW_MAJOR);
        SharedMatrix m = new SharedMatrix();
        // Load as Row Major (default constructor or loadRowMajor)
        m.loadRowMajor(new double[][]{{1.0}});

        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(m),
            "Should throw if matrix is not COLUMN_MAJOR");
    }

    @Test
    void testVecMatMulFailDimensions() {
        // Vector size 3 vs Matrix rows 2
        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(new double[][]{
            {1.0, 2.0},
            {3.0, 4.0}
        }); // 2 rows

        assertThrows(IllegalArgumentException.class, () -> rowVector.vecMatMul(m),
            "Should throw if vector length does not match matrix rows");
    }
}