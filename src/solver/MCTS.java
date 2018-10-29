package solver;

import problem.*;
import simulator.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MCTS {
	
	//fields
	private List<Node> treeNodes;
	private ProblemSpec ps;
    private int goalIndex; //N
    private Level level;
    private int maxNumberOfTimeSteps;
    private State currentState;
    private State rootState;
    private int slipTimePenalty;
    private int breakdownTimePenalty;
    private double discountFactor;
    private List<ActionType> actions = new ArrayList<>();
    private int numberOfCarTypes;
    private int numberOfDrivers;
    private int numberOfTireTypes;
    private int numberOfActions;
    private Terrain[] map; //1D map of environment
   
    private ArrayList<Terrain> terrainTypes = new ArrayList();
    private ArrayList<String> drivers = new ArrayList(); //list of drivers in the order they appear in the inputfile
    private ArrayList<String> cars = new ArrayList(); //list of cars in the order they appear in the inputfile
    private ArrayList<Tire> tires = new ArrayList();; //list of tires in the order they appear in the inputfile
   
    private LinkedHashMap<String, double[]> carToMoveProbability; //list of 12 double values to each car
    private LinkedHashMap<String, double[]> driverToMoveProbability; //list of 12 double values to each driver
    private LinkedHashMap<String, double[]> tireToMoveProbability; //list of 12 double values to each driver
    private double[] terrainToSlipProbability; //hashmap with NT elements. Probability of slipping to each terrain type
    private int[][] fuelConsumption; //Each number is the amount of fuel used by a single moving action (A1) when pressure of tire is 100%. First number:
                                    //first terrain type and first car type. Second: first terrain type, second car type, etc.
   
	
	//constructor
	public MCTS(ProblemSpec ps, State rootState) {
		this.ps = ps;
        this.breakdownTimePenalty=ps.getRepairTime();
        this.slipTimePenalty = ps.getSlipRecoveryTime();
        this.cars=(ArrayList<String>) ps.getCarOrder();
        this.drivers = (ArrayList<String>) ps.getDriverOrder();
        this.tires = (ArrayList<Tire>) ps.getTireOrder();
        this.level = ps.getLevel();
        this.actions = level.getAvailableActions();
        this.carToMoveProbability = ps.getCarMoveProbability();
        this.terrainToSlipProbability = ps.getSlipProbability();
        this.driverToMoveProbability = ps.getDriverMoveProbability();
        this.fuelConsumption = ps.getFuelUsage();
        this.terrainTypes = (ArrayList<Terrain>) level.getTerrainTypes();
        this.map = ps.getEnvironmentMap();
        this.numberOfCarTypes = cars.size();
        this.numberOfDrivers = drivers.size();
        this.numberOfTireTypes = tires.size();
        this.numberOfActions = actions.size();
        this.discountFactor = ps.getDiscountFactor();
        this.goalIndex = ps.getN();
        this.maxNumberOfTimeSteps = ps.getMaxT();
        this.rootState = rootState; //current state. The state where we want to perform an action from
	}
	
	//methods
	
	public Action runMCTS() {
		// running time is limited to 15 seconds per action
    	long start = System.currentTimeMillis();
    	long end = start;
		
		List<Action> actionsTaken = new ArrayList<>();
		Action action = null;
		
		while (end < start + 14000) {
			
			// Selection
			select();
			
			// Expansion
			expand(Node node);
			
			// Simulation
			simulate();
			
			// Backpropagation
			backProgagate(0.0);
			
		}
		
		
		
		return action;
		
	}
	
	/**
	 * PHASE 1 - Selects the most promising node according to 
	 * calculated UCB score
	 * @param 
	 * @return
	 */
	private void select() {
		
	}
	
	/**
	 * PHASE 2 - Expands the node
	 */
	private void expand(Node node) {
		if (node.getNumberOfTimesVisited() == 0) {
			return;
		} else {
			generateChildren(node);
		}
		
	}
	
	private void generateChildren(Node node) {
		for ()
	}
	
	/**
	 * PHASE 3 - Simulates a random rollout
	 */
	private void simulate() {
		boolean terminalNodeFound = false;
		while(!terminalNodeFound) {
			
		}
	}
	
	/**
	 * PHASE 4 - Backpropagates rollout to all parent nodes
	 */
	private void backProgagate(double value) {
		
	}
	
	//Main for testing
	public static void main(String[] args) {
		
	}
}
