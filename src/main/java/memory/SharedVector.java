package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation
        this.vector = vector;
        this.orientation = orientation;
    
    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
        readLock();
        try{
            return vector[index];
        }
        finally{
            readUnlock();
        }

    }

    public int length() {
        // TODO: return vector length
          readLock();
        try{
            return vector.length;
        }
        finally{
            readUnlock();
        }
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
          readLock();
        try{
            return orientation;
        }
        finally{
            readUnlock();
        }
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();

    }

    public void readUnlock() {
        lock.readLock().unlock();

    }

    public void transpose() {
        writeLock();
        try{
            if (this.orientation == VectorOrientation.ROW_MAJOR) 
                this.orientation = VectorOrientation.COLUMN_MAJOR;
            else
                this.orientation = VectorOrientation.ROW_MAJOR;
        }
        finally{
            writeUnlock();
        }
    }

    public void add(SharedVector other) {
        // TODO: add two vectors
        writeLock();
        other.readLock();
        try{
            if(this.orientation == other.orientation && this.vector.length == other.vector.length){
                for(int i = 0; i<this.vector.length; i++){
                    this.vector[i] = this.vector[i] + other.vector[i];
                }
            }
            else{
                throw new IllegalArgumentException("Dimensions mismatch");
            }
        }
        finally{
            other.readUnlock();
            writeUnlock();
        }
    }

    public void negate() {
        // TODO: negate vector
        writeLock();
        try{
            for(int i = 0; i<vector.length; i++)
                this.vector[i] = this.vector[i]*-1;
        }
        finally{
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        // TODO: compute dot product (row · column)
        if(other == null){
            throw new IllegalArgumentException("other is null");
        }
        
        readLock();
        other.readLock();
        try{
            if(this.orientation == VectorOrientation.ROW_MAJOR &&
                other.orientation == VectorOrientation.COLUMN_MAJOR &&
                this.vector.length == other.vector.length){
               double sum = 0.0;
               for(int i = 0; i<vector.length; i++){
                    sum += this.vector[i] * other.vector[i];
               } 
               return sum;
            }
            else{
             throw new IllegalArgumentException("Incompatible vectors for dot product");
            }
        }
        finally{
            other.readUnlock();
            readUnlock();
        }
    }
    
    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
        if (matrix == null)
            throw new IllegalArgumentException("Matrix is null");

        int columns = matrix.length();

        if (columns == 0) {
            readLock();
            try {
                if (this.vector.length != 0)
                    throw new IllegalArgumentException("Dimension mismatch");
                if(this.orientation != VectorOrientation.ROW_MAJOR)
                    throw new IllegalArgumentException("Vector must be ROW_MAJOR for vecMatMul.");
            } finally {
                readUnlock();
            }

            writeLock();
            try {
                this.vector = new double[0];
            } finally {
                writeUnlock();
            }
            return;
        }

        if (matrix.get(0) == null)
            throw new IllegalArgumentException("Invalid matrix");

        double[] temp;
        readLock();
        try {
            if (this.orientation != VectorOrientation.ROW_MAJOR)
                throw new IllegalArgumentException("Vector must be ROW_MAJOR for vecMatMul.");

            if (matrix.getOrientation() != VectorOrientation.COLUMN_MAJOR)
                throw new IllegalArgumentException("Matrix must be COLUMN_MAJOR for vecMatMul.");

            int matrix_rows = matrix.get(0).length();
            if (this.vector.length != matrix_rows)
                throw new IllegalArgumentException("matrix_rows != vector rows");

            temp = new double[columns];
            for (int i = 0; i < columns; i++) {
                temp[i] = this.dot(matrix.get(i));
            }
        } 
        finally {
            readUnlock();
        }

        writeLock();
        try {
            this.vector = temp;
        } 
        finally {
            writeUnlock();
        }
    }
 }

