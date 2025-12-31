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
    @Test
    void testSimpleAddition() {

        double[][] dataA = {{1, 2}, {3, 4}};
        double[][] dataB = {{5, 6}, {7, 8}};
        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);
        ComputationNode root = new ComputationNode("+", Arrays.asList(nodeA, nodeB));
        ComputationNode resultNode = engine.run(root);
        assertEquals(ComputationNodeType.MATRIX, resultNode.getNodeType());
        double[][] result = resultNode.getMatrix();   
        assertEquals(6.0, result[0][0]);
        assertEquals(8.0, result[0][1]);
        assertEquals(10.0, result[1][0]);
        assertEquals(12.0, result[1][1]);
    }
    @Test
    void testSimpleMultiplication() {
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
    @Test
    void testNegate() {
        double[][] data = {{1, -2}, {3, 0}};
        ComputationNode node = new ComputationNode(data);
        ComputationNode root = new ComputationNode("-", Arrays.asList(node));
        ComputationNode resultNode = engine.run(root);
        double[][] result = resultNode.getMatrix();
        assertEquals(-1.0, result[0][0]);
        assertEquals(2.0, result[0][1]);
        assertEquals(-3.0, result[1][0]);
        assertEquals(0.0, result[1][1], 0.0001); 
    }
    @Test
    void testTranspose() {
        double[][] data = {{1, 2, 3}};
        ComputationNode node = new ComputationNode(data);
        ComputationNode root = new ComputationNode("T", Arrays.asList(node));
        ComputationNode resultNode = engine.run(root);
        System.out.println("before::");
        printMatrix(data);
        double[][] result = resultNode.getMatrix();
        printMatrix(result);
        assertEquals(3, result.length);   
        assertEquals(1, result[0].length); 
        assertEquals(1.0, result[0][0]);
        assertEquals(2.0, result[1][0]);
        assertEquals(3.0, result[2][0]);
    }
private void printMatrix(double[][] matrix) {
    System.out.println("--- Matrix Debug ---");
    for (double[] row : matrix) {
        System.out.println(Arrays.toString(row));
    }
}
    @Test
    void testNestedOperations() {
        ComputationNode nodeA = new ComputationNode(new double[][]{{1}});
        ComputationNode nodeB = new ComputationNode(new double[][]{{2}});
        ComputationNode nodeC = new ComputationNode(new double[][]{{4}});
        ComputationNode innerSum = new ComputationNode("+", Arrays.asList(nodeA, nodeB));
                ComputationNode root = new ComputationNode("*", Arrays.asList(innerSum, nodeC));
        ComputationNode resultNode = engine.run(root);
        double[][] result = resultNode.getMatrix();

       assertEquals(12.0, result[0][0]);
    }
    @Test
    void testMultiplicationDimensionMismatch() {
        double[][] dataA = {{1, 2}};
        double[][] dataB = {{1, 2}};
        ComputationNode root = new ComputationNode("*", Arrays.asList(
            new ComputationNode(dataA), 
            new ComputationNode(dataB)
        ));
        assertThrows(RuntimeException.class, () -> engine.run(root));
    }

}