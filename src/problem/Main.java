package problem;

import java.io.IOException;

import solver.Solver;

public class Main {

    public static void main(String[] args) {
 
        ProblemSpec ps;
        try {
        	String inputFileName = args[0];
            ps = new ProblemSpec(inputFileName);
            String outputFileName = args[1];
            Solver rg = new Solver(ps, outputFileName);
            rg.run();
        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!"); 

    }
}
