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
	public static int slipRecoveryTime;
	public static int repairTime;

   
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
		List<Action> actionsTaken = new ArrayList<>(); //list of the total actions taken. Each action is decided by one iteration of MCTS (14 sec)
    	State currentState = State.getStartState(ps.getFirstCarType(), ps.getFirstDriver(), ps.getFirstTireModel());
    	boolean isFinished = false; //complete problem is not finished
    	int rootNodeTimeStep = 0;
    	
    	while(!isFinished) { //outer loop. Run til complete problem is solved
	    	MCTS mcts = new MCTS(ps, currentState, rootNodeTimeStep);
	    	Action a = mcts.runMCTS(); // Action to be taken in this iteration. Each iteration lasts 14 sec.
	    	currentState = sim.step(a);
	    	actionsTaken.add(a);
	    	rootNodeTimeStep = mcts.getCurrentTimeStep();
	    	if(currentState.isInBreakdownCondition()) {
	    		rootNodeTimeStep += repairTime;
	    	}
	    	else if(currentState.isInSlipCondition()) {
	    		rootNodeTimeStep += slipRecoveryTime;
	    	} else {
	    		rootNodeTimeStep++;
	    	}
	    	
	    }
	    
    }

 
 
   
    //main for testing
    public static void main(String[] args) {
       
 
    }
 
}