package problem;

import java.io.IOException;

import solver.Solver;

public class Main {

    public static void main(String[] args) {

        ProblemSpec ps;
        try {
        	String inputFileName = "C:\\Users\\jakob\\git\\Assignment2AI\\examples\\level_1\\input_lvl1.txt";

            ps = new ProblemSpec(inputFileName);
            String outputFileName = "C:\\Users\\jakob\\git\\Assignment2AI\\examples\\level_1\\solvedOutput_lvl1.txt";
            char c = 'M';
            Solver solver = new Solver(ps, outputFileName, c);
        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!");

    }
}
