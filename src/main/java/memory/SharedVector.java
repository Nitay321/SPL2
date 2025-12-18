package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
        readLock();
        try{
        return vector[index];}
        finally {
            readUnlock();
        }
    }

    public int length() {
        
        // TODO: return vector lengt
        readLock();
        try {
            return vector.length;
        } finally {
            readUnlock();
        }
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
    readLock();
    try {
        return orientation;
    } finally {
        readUnlock();}    }

    public void writeLock() {
        lock.writeLock().lock();
        // TODO: acquire write lock
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
        // TODO: release write lock
    }

    public void readLock() {
        lock.readLock().lock();
        // TODO: acquire read lock
    }

    public void readUnlock() {
        lock.readLock().unlock();
        // TODO: release read lock
    }

    public void transpose() {
        writeLock();
        try { 
            if (orientation == VectorOrientation.ROW_MAJOR) {
                orientation = VectorOrientation.COLUMN_MAJOR;
            } else {
                orientation = VectorOrientation.ROW_MAJOR;
            }
        } finally {
            writeUnlock();
        }
        
        
        
        // TODO: transpose vector
    }

    public void add(SharedVector other) {
        writeLock();
        other.readLock();
        try { 
            for (int i = 0; i < vector.length; i++) {
                vector[i] += other.vector[i];
            }
        } finally {
            writeUnlock();
            other.readUnlock();
        }
        
        // TODO: add two vectors
    }

    public void negate() {
        writeLock();
        try { 
            for (int i = 0; i < vector.length; i++) {
                vector[i] = -vector[i];
            }
        } finally {
            writeUnlock();
        }

        // TODO: negate vector
    }

    public double dot(SharedVector other) {
        // TODO: compute dot product (row · column)
        return 0;
    }

    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
    }
}
