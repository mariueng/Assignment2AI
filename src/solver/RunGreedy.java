package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;

import problem.*;
import simulator.*;

public class RunGreedy {
	
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
    
    public static int backStepWeight = -10;
    public static int forwardStepWeight = 10;
    public static int slipbreakStepWeight = -30;
    public static int fuelNeededToMove;
   
	
	public RunGreedy(ProblemSpec ps, String outputFile) {
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
    	State startState = State.getStartState(currentState.getCarType(), currentState.getDriver(), currentState.getTireModel());
    	
    	int indexForBestDriver = findBestDriver();
    	int indexForBestCar = findBestCar();
    	int indexForBestTireType = findBestTireType();
    	
    	String bestCar = cars.get(indexForBestCar);
    	String bestDriver = drivers.get(indexForBestDriver);
    	Tire bestTire = tires.get(indexForBestTireType);
    	
    	//change car type if startCarType is not already best
    	if(!(startState.getCarType().equals(bestCar))){
    		System.out.println("Change car from " + ps.getFirstCarType() + " to " + bestCar);
    		ActionType a = ActionType.CHANGE_CAR;
    		action = new Action(a, bestCar);
    		currentState = sim.step(action);
    		step--;
    	}
    	
    	//change driver if startDriver is not already best
    	if(!(startState.getDriver().equals(bestDriver))) {
    		System.out.println("Change driver from " + ps.getFirstDriver() + " to " + bestDriver);
    		ActionType a = ActionType.CHANGE_DRIVER;
    		action = new Action(a, bestDriver);
    		currentState = sim.step(action);
    		step--;
    	}
    	
    	//change Tire if startTireModel is not already best
    	if(!(startState.getTireModel()==bestTire)) {
    		System.out.println("Change tire from " + ps.getFirstTireModel() + " to " + bestTire);
    		ActionType a = ActionType.CHANGE_TIRES;
    		action = new Action(a, bestTire);
    		currentState = sim.step(action);
    		step--;
    	}
    	

    	while(!finished) {
    		//find out if you are out of fuel
    		boolean notEnoughFuel = false;
    		int fuelNeeded = getFuelConsumption();
    		int fuelAvailable = currentState.getFuel();
    		if(fuelNeeded>fuelAvailable) {
    			notEnoughFuel = true;
    		}
    		if(notEnoughFuel){
    			//fillTank();
    			changeCarTwoTimes();
    		}
    		
    		ActionType a = ActionType.MOVE;
    		action = new Action(a);
    		currentState = sim.step(action);
    		step--;
    		System.out.println(maxNumberOfTimeSteps-step);
    		if(sim.isGoalState(currentState) || step ==0 ) {
    			finished = true;
    			
    		}
    	}
    }

    
    //find the index value of the best car from currentstate
	private int findBestCar() {
		int numberOfCars = cars.size();
		ArrayList<double[]> moveProbsList = new ArrayList<double[]>();
		for(int i = 0;i<numberOfCars;i++) {
			State state = new State(currentState.getPos(), false, false, cars.get(i), ProblemSpec.FUEL_MAX, TirePressure.ONE_HUNDRED_PERCENT, ps.getFirstDriver(), currentState.getTireModel());
			double[] probs = getMoveProbs(state);
			moveProbsList.add(probs);
		}
		ArrayList<Double> scores = new ArrayList<>();
		for(int j = 0;j<numberOfCars;j++) {
			double currentScore = 0;
			double[] probs = moveProbsList.get(j);
			for(int k = 0; k<4;k++) {
				currentScore += backStepWeight*probs[k];
			}
			for(int k = 5; k<10;k++) {
				currentScore += forwardStepWeight*probs[k];
			}
			for(int k = 10; k<12;k++) {
				currentScore += slipbreakStepWeight*probs[k];
			}
			scores.add(currentScore);
		}
		System.out.println("Car scores: " + scores);
		int index = scores.indexOf(Collections.max(scores));
		return index;
	}

	 //find the index value of the best driver from currentstate
	private int findBestDriver() {
		int numberOfDrivers = drivers.size();
		ArrayList<double[]> moveProbsList = new ArrayList<double[]>();
		for(int i = 0;i<numberOfDrivers;i++) {
			State state = new State(currentState.getPos(), false, false, currentState.getCarType(), ProblemSpec.FUEL_MAX, TirePressure.ONE_HUNDRED_PERCENT, drivers.get(i), currentState.getTireModel());
			double[] probs = getMoveProbs(state);
			moveProbsList.add(probs);
		}
		ArrayList<Double> scores = new ArrayList<>();
		for(int j = 0;j<numberOfDrivers;j++) {
			double currentScore = 0;
			double[] probs = moveProbsList.get(j);
			for(int k = 0; k<4;k++) {
				currentScore += backStepWeight*probs[k];
			}
			for(int k = 5; k<10;k++) {
				currentScore += forwardStepWeight*probs[k];
			}
			for(int k = 10; k<12;k++) {
				currentScore += slipbreakStepWeight*probs[k];
			}
			scores.add(currentScore);
		}
		System.out.println("Driver scores: "+scores);
		int index = scores.indexOf(Collections.max(scores));
		return index;
	}
	
	 //find the index value of the best tireModel from currentstate
	private int findBestTireType() {
		int numberOfTires = tires.size();
		ArrayList<double[]> moveProbsList = new ArrayList<double[]>();
		for(int i = 0;i<numberOfTires;i++) {
			State state = new State(currentState.getPos(), false, false, currentState.getCarType(), ProblemSpec.FUEL_MAX, TirePressure.ONE_HUNDRED_PERCENT, currentState.getDriver(), tires.get(i));
			double[] probs = getMoveProbs(state);
			moveProbsList.add(probs);
		}
		ArrayList<Double> scores = new ArrayList<>();
		for(int j = 0;j<numberOfTires;j++) {
			double currentScore = 0;
			double[] probs = moveProbsList.get(j);
			for(int k = 0; k<4;k++) {
				currentScore += backStepWeight*probs[k];
			}
			for(int k = 5; k<10;k++) {
				currentScore += forwardStepWeight*probs[k];
			}
			for(int k = 10; k<12;k++) {
				currentScore += slipbreakStepWeight*probs[k];
			}
			scores.add(currentScore);
		}
		System.out.println("Tire scores: "+scores);
		int index = scores.indexOf(Collections.max(scores));
		return index;
	}
    
   
	//METHOD FOR GETTING MOVE PROBS
	public double[] getMoveProbs(State state) {
		// get parameters of current state
        Terrain terrain = ps.getEnvironmentMap()[state.getPos() - 1];
        int terrainIndex = ps.getTerrainIndex(terrain);
        String car = state.getCarType();
        String driver = state.getDriver();
        Tire tire = state.getTireModel();

        // calculate priors
        double priorK = 1.0 / ProblemSpec.CAR_MOVE_RANGE;
        double priorCar = 1.0 / ps.getCT();
        double priorDriver = 1.0 / ps.getDT();
        double priorTire = 1.0 / ProblemSpec.NUM_TYRE_MODELS;
        double priorTerrain = 1.0 / ps.getNT();
        double priorPressure = 1.0 / ProblemSpec.TIRE_PRESSURE_LEVELS;

        // get probabilities of k given parameter
        double[] pKGivenCar = ps.getCarMoveProbability().get(car);
        double[] pKGivenDriver = ps.getDriverMoveProbability().get(driver);
        double[] pKGivenTire = ps.getTireModelMoveProbability().get(tire);
        double pSlipGivenTerrain = ps.getSlipProbability()[terrainIndex];
        double[] pKGivenPressureTerrain = convertSlipProbs(pSlipGivenTerrain, state);

        // use bayes rule to get probability of parameter given k
        double[] pCarGivenK = bayesRule(pKGivenCar, priorCar, priorK);
        double[] pDriverGivenK = bayesRule(pKGivenDriver, priorDriver, priorK);
        double[] pTireGivenK = bayesRule(pKGivenTire, priorTire, priorK);
        double[] pPressureTerrainGivenK = bayesRule(pKGivenPressureTerrain,
                (priorTerrain * priorPressure), priorK);

        // use conditional probability formula on assignment sheet to get what
        // we want (but what is it that we want....)
        double[] kProbs = new double[ProblemSpec.CAR_MOVE_RANGE];
        double kProbsSum = 0;
        double kProb;
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            kProb = magicFormula(pCarGivenK[k], pDriverGivenK[k],
                    pTireGivenK[k], pPressureTerrainGivenK[k], priorK);
            kProbsSum += kProb;
            kProbs[k] = kProb;
        }

        // Normalize
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            kProbs[k] /= kProbsSum;
        }

        return kProbs;
	}
	
	/**
     * Convert the probability of slipping on a given terrain with 50% tire
     * pressure into a probability list, of move distance versus current
     * terrain and tire pressure.
     *
     * @param slipProb probability of slipping on current terrain and 50%
     *                 tire pressure
     * @return list of move probabilities given current terrain and pressure
     */
    private double[] convertSlipProbs(double slipProb, State state) {

        // Adjust slip probability based on tire pressure
        TirePressure pressure = state.getTirePressure();
        if (pressure == TirePressure.SEVENTY_FIVE_PERCENT) {
            slipProb *= 2;
        } else if (pressure == TirePressure.ONE_HUNDRED_PERCENT) {
            slipProb *= 3;
        }
        // Make sure new probability is not above max
        if (slipProb > ProblemSpec.MAX_SLIP_PROBABILITY) {
            slipProb = ProblemSpec.MAX_SLIP_PROBABILITY;
        }

        // for each terrain, all other action probabilities are uniform over
        // remaining probability
        double[] kProbs = new double[ProblemSpec.CAR_MOVE_RANGE];
        double leftOver = 1 - slipProb;
        double otherProb = leftOver / (ProblemSpec.CAR_MOVE_RANGE - 1);
        for (int i = 0; i < ProblemSpec.CAR_MOVE_RANGE; i++) {
            if (i == ps.getIndexOfMove(ProblemSpec.SLIP)) {
                kProbs[i] = slipProb;
            } else {
                kProbs[i] = otherProb;
            }
        }

        return kProbs;
    }
    
    /**
     * Conditional probability formula from assignment 2 sheet
     *
     * @param pA P(A | E)
     * @param pB P(B | E)
     * @param pC P(C | E)
     * @param pD P(D | E)
     * @param priorE P(E)
     * @return numerator of the P(E | A, B, C, D) formula (still need to divide
     *      by sum over E)
     */
    private double magicFormula(double pA, double pB, double pC, double pD,
                               double priorE) {
        return pA * pB * pC * pD * priorE;
    }
    
    /**
     * Apply bayes rule to all values in cond probs list.
     *
     * @param condProb list of P(B|A)
     * @param priorA prior probability of parameter A
     * @param priorB prior probability of parameter B
     * @return list of P(A|B)
     */
    private double[] bayesRule(double[] condProb, double priorA, double priorB) {

        double[] swappedProb = new double[condProb.length];

        for (int i = 0; i < condProb.length; i++) {
            swappedProb[i] = (condProb[i] * priorA) / priorB;
        }
        return swappedProb;
    }
    /**
     * Get the fuel consumption of moving given the current state
     *
     * @return move fuel consumption for current state
     */
    private int getFuelConsumption() {

        // get parameters of current state
        Terrain terrain = ps.getEnvironmentMap()[currentState.getPos() - 1];
        String car = currentState.getCarType();
        TirePressure pressure = currentState.getTirePressure();

        // get fuel consumption
        int terrainIndex = ps.getTerrainIndex(terrain);
        int carIndex = ps.getCarIndex(car);
        int fuelConsumption = ps.getFuelUsage()[terrainIndex][carIndex];

        if (pressure == TirePressure.FIFTY_PERCENT) {
            fuelConsumption *= 3;
        } else if (pressure == TirePressure.SEVENTY_FIVE_PERCENT) {
            fuelConsumption *= 2;
        }
        return fuelConsumption;
    }
    
    private void fillTank() {
    	System.out.println("Filling the tank..");
		ActionType b = ActionType.ADD_FUEL;
		Action action = new Action(b,(ps.FUEL_MAX)/2);
		currentState = sim.step(action);
    }
    
    private void changeCarTwoTimes() {
    	System.out.println("Change car two times..");
    	ActionType b = ActionType.CHANGE_CAR;
    	String currentCar = currentState.getCarType();
    	ArrayList<String> carsLeft = new ArrayList<>();
    	for(String car:cars) {
    		if(!(car.equals(currentCar))){
    			carsLeft.add(car);
    		}
    	}
    	Action action = new Action(b,carsLeft.get(0));
    	currentState = sim.step(action);
    	action = new Action(b,currentCar);
    	currentState = sim.step(action);
    	
    }
    
}
