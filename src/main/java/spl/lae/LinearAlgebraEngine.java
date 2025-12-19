package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;





    public LinearAlgebraEngine(int numThreads) {
        // TODO: create executor with given thread count
        this.executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        
        ComputationNode nodeToSolve = computationRoot.findResolvable();
        while (nodeToSolve != null) {

        loadAndCompute(nodeToSolve);
        double[][] resultData = leftMatrix.readRowMajor();
        nodeToSolve.resolve(resultData);

        nodeToSolve = computationRoot.findResolvable();
    }
        // TODO: resolve computation tree step by step until final matrix is produced
        
        
        

        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        List<ComputationNode> children = node.getChildren();
        ComputationNode child1 = null; 
        ComputationNode child0 = children.get(0); // left 
        if (children.size() > 1) {
             child1 = children.get(1); // right
        }


        ComputationNodeType type = node.getNodeType();
        List<Runnable> tasks = null;
        // we already checked that the matrecies amount are legal  for each operation
        if (type == ComputationNodeType.MULTIPLY) {
            
            leftMatrix.loadRowMajor(child0.getMatrix());
            rightMatrix.loadColumnMajor(child1.getMatrix());
            if (leftMatrix.get(0).length() != rightMatrix.get(0).length()) {
                throw new IllegalArgumentException("Incompatible matrix sizes for multiplication");
            }

            tasks = createMultiplyTasks();
        }
        else if (type == ComputationNodeType.ADD) {
            leftMatrix.loadRowMajor(child0.getMatrix());
            rightMatrix.loadRowMajor(child1.getMatrix());
            if (leftMatrix.length() != rightMatrix.length() || leftMatrix.get(0).length() != rightMatrix.get(0).length()) {
                throw new IllegalArgumentException("Incompatible matrix sizes for addition");
            }


            tasks = createAddTasks();
        }
        else if (type == ComputationNodeType.NEGATE) {
            leftMatrix.loadRowMajor(child0.getMatrix());
            tasks = createNegateTasks();
        }
        else if (type == ComputationNodeType.TRANSPOSE) {
            leftMatrix.loadColumnMajor(child0.getMatrix());
            tasks = createTransposeTasks();
        }
        executor.submitAll(tasks);

        

    }

    public List<Runnable> createAddTasks() {
        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();
        for (int i = 0; i < rows; i++) { 
            final int index = i; 
            Runnable task = () -> {
                SharedVector v1  = leftMatrix.get(index);
                SharedVector v2  = rightMatrix.get(index);
                v1.add(v2);
            };
            tasks.add( task);

                } 





        // TODO: return tasks that perform row-wise addition
        return tasks; 
    }

    public List<Runnable> createMultiplyTasks() {
    List<Runnable> tasks = new ArrayList<>();
    int rows = leftMatrix.length();

    for (int i = 0; i < rows; i++) {
        final int rowIndex = i;
        tasks.add(() -> {
            leftMatrix.get(rowIndex).vecMatMul(rightMatrix);
        });
        }

    return tasks;
    }

    public List<Runnable> createNegateTasks() {
        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();
        for (int i = 0; i < rows; i++) {
            final int index = i;
            tasks.add(() -> {
                SharedVector v = leftMatrix.get(index);
                v.negate();
            });
            
        }
        
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        // matrix was loaded as column-major already 
        return java.util.Collections.emptyList();
    }

    public String getWorkerReport() {
        
        // TODO: return summary of worker activity
        return executor.getWorkerReport();
    }

    public void shutdown() throws InterruptedException {
        this.executor.shutdown();
    }   




}
