package solver;

import java.util.ArrayList;
import java.util.List;

import problem.Action;
import problem.ActionType;
import simulator.State;

public class A1Node extends Node {
	
	private double probability;
	
	public A1Node(Node parent, State state, double probability) {
		super(parent, null,state);
		this.probability = probability; //probability for ending up in this node after taken an A1 action in the parent node
		
	}
	
	//getProbability
	public double getProbability() {
		return probability;
	}
	
	//toString
	public String toString() {
		String result = "(Node type: ";

		if(this.getParentNode() == null) {
			result += "root | ";
			}
		else{
			result += "A1 | p: " + probability + " | ";
		}

		result += "d: " + this.getDepth() + " | s: " + this.getTotalScore() + " | UCB: " + this.getUcbValue() + " | n: " + this.getNumberOfTimesVisited() +")";
		result += "\n with " + getNodeState();
		return result;
	}

}
