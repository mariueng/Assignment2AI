package solver;

import problem.*;
import simulator.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import com.sun.javafx.binding.SelectBinding.AsLong;

public class MCTS {
	
	//fields
	private ProblemSpec ps;
	private List<Node> treeNodes;
    private int goalIndex; //N
    private Level level;
    private int maxNumberOfTimeSteps;
    private State currentState;
    private State rootState;
    private int slipTimePenalty;
    private int breakdownTimePenalty;
    private double discountFactor;
    
    private List<ActionType> actionTypes = new ArrayList<>();
    private List<Action> rootActionSpace = new ArrayList<>(); // [a1, a21, a22, a31, a32, a33, .... anm]
    private int numberOfPossibleAction = rootActionSpace.size();
    private int numberOfCarTypes;
    private int numberOfDrivers;
    private int numberOfTireTypes;
    private int numberOfActionTypes;
    private Terrain[] map; //1D map of environment
   
    private ArrayList<Terrain> terrainTypes = new ArrayList();
    private ArrayList<String> drivers = new ArrayList(); //list of drivers in the order they appear in the inputfile
    private ArrayList<String> cars = new ArrayList(); //list of cars in the order they appear in the inputfile
    private ArrayList<Tire> tires = new ArrayList();; //list of tires in the order they appear in the inputfile
    private ArrayList<TirePressure> tirePressures = new ArrayList<>(); //list of tirePressures
    
    private LinkedHashMap<String, double[]> carToMoveProbability; //list of 12 double values to each car
    private LinkedHashMap<String, double[]> driverToMoveProbability; //list of 12 double values to each driver
    private LinkedHashMap<String, double[]> tireToMoveProbability; //list of 12 double values to each driver
    private double[] terrainToSlipProbability; //hashmap with NT elements. Probability of slipping to each terrain type
    private int[][] fuelConsumption; //Each number is the amount of fuel used by a single moving action (A1) when pressure of tire is 100%. First number:
                                    //first terrain type and first car type. Second: first terrain type, second car type, etc.
    
    private ArrayList<Double> utilities = new ArrayList<>(); //list of accumulated utilites for each action(Aactiontype, parameter). Size: numberOfPossibleActions
    private ArrayList<Node> nodes = new ArrayList<>(); //list of all nodes in the MCTS-tree. NB. does not contain nodes/states genereted in the simulate-step of the inner loop.
    private Node rootNode;  //rootNode. Made from the state we want to use MCTS to find the best action from
    
	//constructor
	public MCTS(ProblemSpec ps, State rootState) {
		this.rootNode = new Node(rootState);
		this.ps = ps;
        this.breakdownTimePenalty=ps.getRepairTime();
        this.slipTimePenalty = ps.getSlipRecoveryTime();
        this.cars=(ArrayList<String>) ps.getCarOrder();
        this.drivers = (ArrayList<String>) ps.getDriverOrder();
        this.tires = (ArrayList<Tire>) ps.getTireOrder();
        this.level = ps.getLevel();
        this.actionTypes = level.getAvailableActions(); //actiontypes in the particular level
        
        this.carToMoveProbability = ps.getCarMoveProbability();
        this.terrainToSlipProbability = ps.getSlipProbability();
        this.driverToMoveProbability = ps.getDriverMoveProbability();
        this.fuelConsumption = ps.getFuelUsage();
        this.terrainTypes = (ArrayList<Terrain>) level.getTerrainTypes();
        this.map = ps.getEnvironmentMap();
        this.numberOfCarTypes = cars.size();
        this.numberOfDrivers = drivers.size();
        this.numberOfTireTypes = tires.size();
        this.numberOfActionTypes = actionTypes.size();
        this.discountFactor = ps.getDiscountFactor();
        this.goalIndex = ps.getN();
        this.maxNumberOfTimeSteps = ps.getMaxT();
        this.rootState = rootState; //current state. The state where we want to perform an action from
        this.rootActionSpace = makeActionSpace(rootState);
	}
	
	//methods
	
	public Action runMCTS() { //delivere one action after 14 seconds of MCTS-inner loops
    	long start = System.currentTimeMillis();
    	long end = start;

		generateChildren(rootNode); //means basiacallly: make a node for each possible action(actiontype, outcome/parameter) from rootNode
		nodes.add(rootNode);
		while (end < start + 14000) { //inner loop
			// Selection
			Node selectedNode = select(); //select the node with the highest ucb-value
			// Simulation
			double reward = rollout(selectedNode);
			
			// Backpropagation
			backProgagate(selectedNode, reward);
			
		}
		makeUtilities();
		int index = utilities.indexOf(Collections.max(utilities)); //index value of the best action from rootNode
		Action action = rootActionSpace.get(index);
		return action;
	}
	
	/**
	 * PHASE 1 - Select the most promising node according to calculated UCB score
	 * The selected node will be rolled out
	 * @param 
	 * @return
	 */
	private Node select() {
		Node result = null; //the node you want to expand/rollout. Is it has never been visited before, go directly to rollout
		Node currentNode = nodes.get(0); //rootNode
		while(true) {
			boolean isLeafNode;
			if(currentNode.getChildren().size()==0) {
				isLeafNode = true;
			}else {isLeafNode = false;}
			
			if(isLeafNode) {//currentnode is a leaf node
				int visited = currentNode.getNumberOfTimesVisited();
				if(visited == 0) {
					result = currentNode;
					break; //first time we visit a leafnode -> rollout
				}else { 
					generateChildren(currentNode); //second time we visit a leafnode -> expand (phase 2)
					result = currentNode.getChildren().get(0); //we want to rollout the first element of the new children
					break;
				}
			}
			else { //not leaf node
				ArrayList<Double> ucbValuesForChildren = new ArrayList<>();
				for(Node n:currentNode.getChildren()) {
					ucbValuesForChildren.add(n.getUcbValue());
				}
				int index = ucbValuesForChildren.indexOf(Collections.max(ucbValuesForChildren));
				currentNode = currentNode.getChildren().get(index);
			}
		}
		return result;
	}

	
	//method for generating node-children (step 2 in original MCTS)
	private void generateChildren(Node node) { //generate children nodes for each available action(Action type, outcome/parameter) of a parent node
		ArrayList<Action> actionSpace = makeActionSpace(node.getNodeState());
		for(Action action :actionSpace) {
			if(action.getActionType()==ActionType.MOVE) {
				for(int k=-4; k<7;k++) { //add childnode for each k, when action = A1
					MCTSStateSimulator sim = new MCTSStateSimulator(node, k);
					double prob = sim.getProbability();
					State state = sim.returnState();
					Node childNode = new Node(node, state, prob);
					nodes.add(childNode);
				}
			}
			else {
				MCTSStateSimulator sim = new MCTSStateSimulator(node, action);
				State state = sim.returnState();
				Node chilNode = new Node(node, action, state);
				nodes.add(chilNode); //node added to MCTS-tree
			}
		}
	}
	

	
	/**
	 * PHASE 2 - Simulates a random rollout
	 */
	private double rollout(Node node) {
		Node currentNode = node; //Randomly generated child node from prevoious currentNode
		double reward = 0;
		while(true) {
			Random rand = new Random();
			ArrayList<Action> actionSpace = makeActionSpace(currentNode.getNodeState());
			int actionIndexForRandomAction = rand.nextInt(actionSpace.size()); //use heuristic so that the action is not random? Notice that A1 has the twelve first actions, so the chance of hitting A1 is bigger than the other
			Action action = actionSpace.get(actionIndexForRandomAction);
			MCTSStateSimulator sim = new MCTSStateSimulator(currentNode, action);
			State state = sim.returnState();
			int timeStep = sim.getTimeStepNumber();
			currentNode = new Node(currentNode, state);
			if(currentNode.getNodeState().getPos() == goalIndex) { //found goal
				reward += 100/timeStep; //finding goalstate in 1 action should be better than finding it after 10 actions
				return reward;
			}
			else if(timeStep >= maxNumberOfTimeSteps) { //did not find goal
				return reward; //return 0 because the terminal state was a failure
			}
			
			
		}
	}
	
	/**
	 * PHASE 3 - Backpropagates rollout to all parent nodes
	 * NB: remember to plus 1 to numberOfVisited to all "parents"!
	 */
	private void backProgagate(Node superNode, double reward) {
		Node currentNode = superNode;
		while(!(currentNode==null)) {
			int prevNumberOfTimesVisited = currentNode.getNumberOfTimesVisited();
			currentNode.setNumberOfTimesVisited(prevNumberOfTimesVisited +1);//update number of times visited
			double prevTotalScore = currentNode.getTotalScore();
			currentNode.setTotalScore(prevTotalScore + reward); //update total score
			currentNode.calculateUcbScore(); //update ucb-score
			currentNode = currentNode.getParentNode();
		}
	}
	
	
	//METHOD FOR MAKING UTILITIES
	//Used when MCTS has completed it's 14 seconds of running a
	private void makeUtilities() {
		double a1Utility =  0;
		ArrayList<Node> actions = (ArrayList<Node>) this.rootNode.getChildren();
		for(int i = 0;i<12;i++) {
			Node kNode = actions.get(i);
			double kNodeValue = kNode.getTotalScore()*kNode.getProbability(); //scaling down value with the probability of actually getting intoo that node
			a1Utility += kNodeValue;
		}
		utilities.add(a1Utility);
		for(int j = 12;j<rootActionSpace.size();j++) {
			utilities.add(rootNode.getChildren().get(j).getTotalScore());
		}
	}
	
	//Make actionSpace for a state.  
	private ArrayList<Action> makeActionSpace(State state){
		ArrayList<Action> actionSpace = new ArrayList<>();
		for(int i =0;i<numberOfActionTypes;i++) {
			if(i==0) { //add Move action
				Action action = new Action(actionTypes.get(0));
				actionSpace.add(action); //added MOVE-action to rootActionSpace
			}
			else if(i==1) {	//add change cartype actions
				String startCar = state.getCarType();
				for(int j = 0; j<numberOfCarTypes;j++) {
					if(!cars.get(j).equals(startCar)){ //add all cars without the current startCar
						Action action = new Action(actionTypes.get(i), cars.get(j));
						actionSpace.add(action);
					}
				}
			}
			else if(i==2) { //add change driver actions
				String startDriver = state.getDriver();
				for(int j =0; j<numberOfDrivers;j++) {
					if(!drivers.get(j).equals(startDriver)) {
						Action action = new Action(actionTypes.get(i), drivers.get(j));
						actionSpace.add(action);
					}
				}
			}
			else if(i == 3) { //add change tire actions
				Tire startTire = state.getTireModel();
				for(Tire t:tires) {
					if(!(t==startTire)) {
						Action action = new Action(actionTypes.get(i), t);
						actionSpace.add(action);
					}
				}
			}
			else if(i==4) {
				if(ps.getLevel().getLevelNumber() ==1) {
					continue; //adding fuel
				}
				else {
					continue; //SHOULD WE EVEN USE THIS ACTION??
				}
			}
			else if(i==5) { //add change tire pressure actions
				if(ps.getLevel().getLevelNumber()==1) {
					continue; //changing pressure is not an option in level 1
				}else {
					TirePressure currentPressure = state.getTirePressure();
					Action a1 = new Action(actionTypes.get(i), TirePressure.SEVENTY_FIVE_PERCENT);
					Action a2 = new Action(actionTypes.get(i), TirePressure.FIFTY_PERCENT);
					Action a3 = new Action(actionTypes.get(i), TirePressure.ONE_HUNDRED_PERCENT);
					List<Action> allPressureActions = new ArrayList<>();
					allPressureActions.addAll(Arrays.asList(a1,a2,a3));
					for(Action a : allPressureActions) {
						if(a.getTirePressure()==currentPressure) {
							allPressureActions.remove(allPressureActions.indexOf(a)); //remove current Tire Pressure
						}
						else {
							actionSpace.add(a);
						}
					}
					
					
				}
			}
		}
		return actionSpace;
	} //end of method makeActionSpace
	
	
	//Main for testing
	public static void main(String[] args) {
		
	}
}
