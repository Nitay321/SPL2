package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
        this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors
        if(!validMatrix(matrix))
            throw new IllegalArgumentException("Matrix is not valid");
        
        this.vectors = new SharedVector[matrix.length];
        for(int i = 0; i<matrix.length; i++){
            this.vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR); 
        }
    }

    public void loadRowMajor(double[][] matrix) {
        // TODO: replace internal data with new row-major matrix
        
        if(!validMatrix(matrix))
            throw new IllegalArgumentException("Matrix is not valid");
         
        SharedVector[] new_vectors = new SharedVector[matrix.length];
        for(int i = 0; i<matrix.length; i++){
            new_vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR); 
        }
        this.vectors = new_vectors;
    
        
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix
        if(!validMatrix(matrix))
            throw new IllegalArgumentException("Matrix is not valid");
        

        int rows = matrix.length;
        int columns = matrix[0].length;
        SharedVector[] new_vectors = new SharedVector[columns];
                
        for(int i = 0; i<columns; i++){
            double[] temp = new double[rows];

            for(int j = 0; j<rows; j++){
                temp[j] = matrix[j][i]; 
            }
            new_vectors[i] = new SharedVector(temp, VectorOrientation.COLUMN_MAJOR);  
        }
        this.vectors = new_vectors;
    }
    // i added this function
    private boolean validMatrix(double[][] matrix){
         if(matrix == null || matrix.length == 0 || matrix[0] == null)
            return false;
        int columns = matrix[0].length;
         for(int i = 1; i < matrix.length; i++){
            if(matrix[i] == null || columns != matrix[i].length){
                return false;
            }
         }
         return true;
    }
            
        
    
    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        SharedVector[] current_vectors = this.vectors;
        if (current_vectors == null || current_vectors.length == 0) 
            return new double[0][0]; 
        
        acquireAllVectorReadLocks(current_vectors);

        try{
            if(this.getOrientation() == VectorOrientation.COLUMN_MAJOR){
                int columns = current_vectors.length;
                int rows = current_vectors[0].length();
                
                double[][] matrix = new double[rows][columns];
                
                for(int j = 0; j<columns; j++){
                    for(int i = 0; i<rows; i++)
                        matrix[i][j] = current_vectors[j].get(i);
                } 
                return matrix;
            }
            else{
                int rows = current_vectors.length;
                int columns = current_vectors[0].length();
                
                double[][] matrix = new double[rows][columns];
                
                for(int i = 0; i<rows; i++){
                    for(int j = 0; j<columns; j++)
                        matrix[i][j] = current_vectors[i].get(j);
                } 
                return matrix;
            }           
        }
        finally{
            releaseAllVectorReadLocks(current_vectors);
        }
    }

    public SharedVector get(int index) {
        // TODO: return vector at index
        SharedVector[] current_vectors = this.vectors;
        if (current_vectors == null || index < 0 || index >= current_vectors.length) 
            throw new IllegalArgumentException("invalid data");
        return current_vectors[index];
    }

    public int length() {
        // TODO: return number of stored vectors
        SharedVector[] current_vectors = this.vectors;
        return current_vectors.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        SharedVector[] current_vectors = this.vectors;
        if (current_vectors == null || current_vectors.length == 0) 
            return VectorOrientation.ROW_MAJOR;  
        return current_vectors[0].getOrientation();

    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
        for(int i = 0; i<vecs.length; i++)
            vecs[i].readLock();

    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
         for(int i = 0; i<vecs.length; i++)
            vecs[i].readUnlock();
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
         for(int i = 0; i<vecs.length; i++)
            vecs[i].writeLock();
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
        for(int i = 0; i<vecs.length; i++)
            vecs[i].writeUnlock();
    }
}
