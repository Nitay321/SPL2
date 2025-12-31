package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    /* We use ReentrantReadWriteLock to allow multiple threads to read the vector 
       simultaneously, while allowing only one thread to write to it at a time.
       Examples of using read and write in functions:
       Read: get, length, getOrientation, dot, add, vecMatMul
       Write: negate, transpose, add, vecMatMul
    */
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
       
        
        /* 
        In the add function, we have to read the other object and also write to "this". 
        Because we need two locks, there is a risk of deadlock if "other" adds "this" 
        and "this" adds "other" at the same time. That is why we use the hash code 
        to acquire the locks in a global order that doesn't depend on the direction 
        of the add operation.     
         */   
        
        if (System.identityHashCode(this) <= System.identityHashCode(other)) {
            this.writeLock();
            try{
                other.readLock();
                try{
                    addOther(other);
                }
                finally{
                    other.readUnlock();
                }
            }
            finally{
                this.writeUnlock();
            }
        }
        else{
            other.readLock();           
            try{
                this.writeLock();
                try{
                    addOther(other);
                }
                finally{
                    this.writeUnlock();
                }
            }
            finally{
                other.readUnlock();
            }
        }
    }
    private void addOther(SharedVector other){
        if(this.orientation == other.orientation && this.vector.length == other.vector.length){
                for(int i = 0; i<this.vector.length; i++){
                    this.vector[i] = this.vector[i] + other.vector[i];
                }
            }
            else{
                throw new IllegalArgumentException("Dimensions mismatch");
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


        
        double[] temp;  /* In this function we calculate the result in a temp array using only read lock, 
                        so we dont block other threads during the heavy math.  
                        We only take the write lock at the very end to update the vector quickly. 
                        */ 



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