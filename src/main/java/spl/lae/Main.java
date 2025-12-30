package spl.lae;
import java.io.IOException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
int numThreads = Integer.parseInt(args[0]);
        String inputPath = args[1];
        String outputPath = args[2];
      LinearAlgebraEngine engine = new LinearAlgebraEngine(numThreads);
      try {
      InputParser parser = new InputParser();
      ComputationNode root = parser.parse(inputPath); 
      ComputationNode resultNode = engine.run(root); 


      OutputWriter.write(resultNode.getMatrix(), outputPath);      
    }


  catch (Exception e) {
    // This catches IllegalArgumentException, ParseException, and anything else!
    OutputWriter.write(e.getMessage(), outputPath);
      
}
finally {
try {
        engine.shutdown();
    } catch (InterruptedException ignored) {}

}
}
} 