package problem;

import java.io.IOException;

import solver.Solver;

public class Main {

    public static void main(String[] args) {
 
        ProblemSpec ps;
        try {
        	String inputFileName = "C:\\Users\\jakob\\git\\Assignment2AI\\examples\\level_5\\input_lvl5_2.txt";
            ps = new ProblemSpec(inputFileName);
            String outputFileName = "C:\\Users\\jakob\\git\\Assignment2AI\\examples\\level_5\\solvedOutput_lvl5.txt";
            char c = 'G';
            Solver solver = new Solver(ps, outputFileName, c);
        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!"); 

    }
}
