package spl.lae;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ComputationNode;
import parser.ComputationNodeType;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinearAlgebraEngineTest {

    private LinearAlgebraEngine engine;
    private final int NUM_THREADS = 4;

    // --- Setup & Teardown ---
    @BeforeEach
    void setUp() {
        engine = new LinearAlgebraEngine(NUM_THREADS);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (engine != null) {
            engine.shutdown();
        }
    }

    // --- TEST 1: Simple Matrix Addition (A + B) ---
    @Test
    void testSimpleAddition() {
        // Construct leaf nodes (Matrices)
        double[][] dataA = {{1, 2}, {3, 4}};
        double[][] dataB = {{5, 6}, {7, 8}};
        
        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);

        // Construct Root (A + B)
        ComputationNode root = new ComputationNode("+", Arrays.asList(nodeA, nodeB));

        // Execute
        ComputationNode resultNode = engine.run(root);

        // Verify
        assertEquals(ComputationNodeType.MATRIX, resultNode.getNodeType());
        double[][] result = resultNode.getMatrix();
        
        // Expected: {{6, 8}, {10, 12}}
        assertEquals(6.0, result[0][0]);
        assertEquals(8.0, result[0][1]);
        assertEquals(10.0, result[1][0]);
        assertEquals(12.0, result[1][1]);
    }

    // --- TEST 2: Simple Multiplication (A * B) ---
    @Test
    void testSimpleMultiplication() {
        // A (1x2) * B (2x2) -> Result (1x2)
        // [1, 2] * [[3, 4], [5, 6]] = [1*3 + 2*5, 1*4 + 2*6] = [13, 16]
        
        double[][] dataA = {{1, 2}};
        double[][] dataB = {{3, 4}, {5, 6}};

        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);
        ComputationNode root = new ComputationNode("*", Arrays.asList(nodeA, nodeB));

        ComputationNode resultNode = engine.run(root);
        double[][] result = resultNode.getMatrix();

        assertEquals(1, result.length);
        assertEquals(2, result[0].length);
        assertEquals(13.0, result[0][0]);
        assertEquals(16.0, result[0][1]);
    }

// --- TEST 6: Negate (-A) ---
    @Test
    void testNegate() {
        // Input: [[1, -2], [3, 0]]
        // Expected: [[-1, 2], [-3, 0]]
        double[][] data = {{1, -2}, {3, 0}};

        ComputationNode node = new ComputationNode(data);
        // The "-" operator triggers createNegateTasks()
        ComputationNode root = new ComputationNode("-", Arrays.asList(node));

        ComputationNode resultNode = engine.run(root);
        double[][] result = resultNode.getMatrix();

        // Verify values flipped signs
        assertEquals(-1.0, result[0][0]);
        assertEquals(2.0, result[0][1]);
        assertEquals(-3.0, result[1][0]);
        // 0.0 negated is still 0.0 (or -0.0), checking with delta for safety
        assertEquals(0.0, result[1][1], 0.0001); 
    }


    // --- TEST 3: Transpose (T(A)) ---
    @Test
    void testTranspose() {
        // T([[1, 2, 3]]) -> [[1], [2], [3]]
        double[][] data = {{1, 2, 3}};
        
        ComputationNode node = new ComputationNode(data);
        ComputationNode root = new ComputationNode("T", Arrays.asList(node));

        ComputationNode resultNode = engine.run(root);
        System.out.println("before::");
        printMatrix(data);
        double[][] result = resultNode.getMatrix();
        printMatrix(result);
        

        assertEquals(3, result.length);    // 3 rows
        assertEquals(1, result[0].length); // 1 column
        assertEquals(1.0, result[0][0]);
        assertEquals(2.0, result[1][0]);
        assertEquals(3.0, result[2][0]);
    }
private void printMatrix(double[][] matrix) {
    System.out.println("--- Matrix Debug ---");
    for (double[] row : matrix) {
        System.out.println(Arrays.toString(row));
    }
    System.out.println("--------------------");
}

    // --- TEST 4: Nested Operation ( (A + B) * C ) ---
    @Test
    void testNestedOperations() {
        // A = [[1]], B = [[2]] -> (A+B) = [[3]]
        // C = [[4]]
        // Result = [[3]] * [[4]] = [[12]]

        ComputationNode nodeA = new ComputationNode(new double[][]{{1}});
        ComputationNode nodeB = new ComputationNode(new double[][]{{2}});
        ComputationNode nodeC = new ComputationNode(new double[][]{{4}});

        // Inner: (A + B)
        ComputationNode innerSum = new ComputationNode("+", Arrays.asList(nodeA, nodeB));
        
        // Root: Inner * C
        ComputationNode root = new ComputationNode("*", Arrays.asList(innerSum, nodeC));

        ComputationNode resultNode = engine.run(root);
        double[][] result = resultNode.getMatrix();

        assertEquals(12.0, result[0][0]);
    }

    // --- TEST 5: Dimension Mismatch Error ---
    @Test
    void testMultiplicationDimensionMismatch() {
        // 1x2 * 1x2 -> Error (Cols A != Rows B)
        double[][] dataA = {{1, 2}};
        double[][] dataB = {{1, 2}};

        ComputationNode root = new ComputationNode("*", Arrays.asList(
            new ComputationNode(dataA), 
            new ComputationNode(dataB)
        ));

        // Expect RuntimeException or IllegalArgumentException propagated from engine
        assertThrows(RuntimeException.class, () -> engine.run(root));
    }
}