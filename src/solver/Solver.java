package solver;
import simulator.*;
import sun.java2d.pipe.hw.AccelTypedVolatileImage;
import problem.*;
 
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
 
public class Solver {
   
   
    //fields
	private ProblemSpec ps;
	private String outputFileName;
	private Simulator sim;
   
    //constructor
    public Solver(ProblemSpec ps, String outputFileName, char c) {
    	this.ps = ps;
    	this.outputFileName = outputFileName;
    	this.sim = new Simulator(ps, outputFileName);
    	if (c == 'r') {
    		RunRandom rr = new RunRandom(ps, outputFileName);
    	} else {
    		runMCTS(ps);
    	}
    }
   
   
    	//METHODS
    
    /**
     * Method for solving the task in a smart way
     * @param ouputFileName
     */
    private void runMCTS(ProblemSpec ps) {
    	State currentState = State.getStartState(ps.getFirstCarType(), 
    			ps.getFirstDriver(), ps.getFirstTireModel());
    	boolean isFinished = false;
    	
    	while(!isFinished) { //outer loop. Run til complete problem is solved
	    	MCTS mcts = new MCTS(ps, currentState);
	    	Action a = mcts.runMCTS(); // Action to be taken in this iteration. Each iteration lasts 14 sec.
	    	currentState = sim.step(a);
	    }
	    
    }

 
 
   
    //main for testing
    public static void main(String[] args) {
       
 
    }
 
}