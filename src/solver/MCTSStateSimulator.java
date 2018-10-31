package solver;
import problem.*;
import simulator.*;

public class MCTSStateSimulator {
	
	/**
	 * This class will have input: node (parentNode), action (from that parentNode)
	 * and output: next state
	 * 
	 * When action is A1 (Move), it takes probabilities into account
	 */
	
	//fields
	private Node parentNode;
	private Action action;
	private double probability; //only interesting if action = A1
	
	//constructor for A1. k e [-4,7]
	public MCTSStateSimulator(Node parentNode, int k) {
		this.parentNode = parentNode;
		this.probability = calculateProb(k);
	}
	
	//constructor for the rest
	public MCTSStateSimulator(Node parentNode, Action action) {
		this.parentNode = parentNode;
		this.action = action;
	}
	
	//methods
	
	/**
	 * 
	 * Method for returning result state
	 */
	public State returnState() {
		State result = null;
		
		return result;
	}

	
	/**
	 * Method for getting the probability for a given k from given parentNode
	 */
	private double calculateProb(int k) {
		double result = 0;
		
		return result;
	}
	
	/**
	 * Method for reting timestep number
	 * @return
	 */
	public int getTimeStepNumber() {
		int result = 0;
		return result;
	}
	
	
	
	//getProb
	public double getProbability() {
		return this.probability;
	}
	
	//Main for testing
	
}
