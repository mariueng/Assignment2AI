package solver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import problem.*;
import simulator.*;

public class RunRandom {
	
	//fields
	private Simulator sim;
	private List<Node> treeNodes;
	private ProblemSpec ps;
    private int goalIndex; //N
    private Level level;
    private int maxNumberOfTimeSteps;
    private State currentState;
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
   
	
	public RunRandom(ProblemSpec ps, String outputFile) {
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
        this.currentState = State.getStartState(ps.getFirstCarType(), ps.getFirstDriver(), ps.getFirstTireModel());
        this.sim = new Simulator(ps, outputFile);
        run();
	}
	
	/**
     * Methods for solving the task by choosing actions uniformly at random
     * @param ouputFileName
     */
    
    private void run() {
    	boolean finished = false;
    	Random rand = new Random();
    	Action action = null;
    	int step = maxNumberOfTimeSteps;
    	while(!finished) {
    		int actionIndex = rand.nextInt(numberOfActions);
    		if(actionIndex==0) {
    			ActionType a = ActionType.MOVE;
    			action = new Action(a);
    		}
    		else if(actionIndex == 1) {
    			ActionType a = ActionType.CHANGE_CAR;
        		int carIndex = rand.nextInt(numberOfCarTypes);
        		String car = cars.get(carIndex);
        		action = new Action(a, car);
    		}
    		else if(actionIndex == 2) {
    			ActionType a = ActionType.CHANGE_DRIVER;
        		int index = rand.nextInt(numberOfDrivers);
        		String driver = drivers.get(index);
        		action = new Action(a, driver);
    		}
    		else {
    			ActionType a = ActionType.CHANGE_TIRES;
        		int index = rand.nextInt(numberOfDrivers);
        		Tire tire = tires.get(index);
        		action = new Action(a, tire);
    		}
    		currentState = sim.step(action);
    		step--;
    		System.out.println(maxNumberOfTimeSteps-step);
    		if(sim.isGoalState(currentState) || step ==0 ) {
    			finished = true;
    			
    		}
    	}
    }
}
