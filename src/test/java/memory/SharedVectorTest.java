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
        rowVector = new SharedVector(data.clone(), VectorOrientation.ROW_MAJOR);
        colVector = new SharedVector(data.clone(), VectorOrientation.COLUMN_MAJOR);
    }


    @Test
    void testConstructorAndAccessors() {
        assertEquals(3, rowVector.length(), "Length should be 3");
        assertEquals(VectorOrientation.ROW_MAJOR, rowVector.getOrientation());
        assertEquals(1.0, rowVector.get(0));
        assertEquals(3.0, rowVector.get(2));
    }


    @Test
    void testTranspose() {
        rowVector.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, rowVector.getOrientation());
        rowVector.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, rowVector.getOrientation());
    }


    @Test
    void testNegate() {
        rowVector.negate();
        assertEquals(-1.0, rowVector.get(0));
        assertEquals(-2.0, rowVector.get(1));
        assertEquals(-3.0, rowVector.get(2));
    }


    @Test
    void testAddSuccess() {
        SharedVector other = new SharedVector(new double[]{4.0, 5.0, 6.0}, VectorOrientation.ROW_MAJOR);
        rowVector.add(other);

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


    @Test
    void testDotProductSuccess() {
        double result = rowVector.dot(colVector);
        assertEquals(14.0, result, 0.0001);
    }

    @Test
    void testDotProductFailOrientation() {
        SharedVector row2 = new SharedVector(data.clone(), VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> rowVector.dot(row2), 
                "Dot product should fail if second vector is not COLUMN_MAJOR");
    }

    @Test
    void testVecMatMulNull() {
        assertThrows(IllegalArgumentException.class, () -> rowVector.vecMatMul(null),
            "Should throw exception when matrix is null");
    }

    @Test
    void testVecMatMulEmptyMatrix_NonEmptyVector() {
        SharedMatrix emptyMatrix = new SharedMatrix();
        
        assertThrows(IllegalArgumentException.class, 
            () -> rowVector.vecMatMul(emptyMatrix),
            "Should throw exception because vector has data {1,2,3} but matrix is empty"
        );
    }

    @Test
    void testVecMatMulEmptyMatrix_EmptyVector() {
        SharedVector emptyVec = new SharedVector(new double[]{}, VectorOrientation.ROW_MAJOR);
        SharedMatrix emptyMatrix = new SharedMatrix();
        
        assertDoesNotThrow(() -> emptyVec.vecMatMul(emptyMatrix));
        assertEquals(0, emptyVec.length());
    }

    @Test
    void testVecMatMulSuccess() {

        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        
        SharedMatrix m = new SharedMatrix();
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
        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(new double[][]{{1},{2}});
        
        assertThrows(IllegalArgumentException.class, () -> colVector.vecMatMul(m),
            "Should throw if vector is not ROW_MAJOR");
    }

    @Test
    void testVecMatMulFailMatrixOrientation() {
        SharedVector v = new SharedVector(new double[]{1.0}, VectorOrientation.ROW_MAJOR);
        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(new double[][]{{1.0}});

        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(m),
            "Should throw if matrix is not COLUMN_MAJOR");
    }

    @Test
    void testVecMatMulFailDimensions() {
        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(new double[][]{
            {1.0, 2.0},
            {3.0, 4.0}
        }); 

        assertThrows(IllegalArgumentException.class, () -> rowVector.vecMatMul(m),
            "Should throw if vector length does not match matrix rows");
    }
}