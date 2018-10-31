package solver;
import java.util.ArrayList;
import java.util.List;

import problem.Action;
import simulator.State;

public class Node {
	
	private final static int bigValue = 100000;
	
	//fields
	private Action prevAction;
	private Node parentNode;
	private int depth;
	private double totalScore; //accumalated total score
	private double ucbValue;
	private int numberOfTimesVisited; //how many times have we visited this node. Only interesting for the root's children
	private State nodeState;
	private boolean isTerminalState;
	private final int explConstant = 2; //This constant can be changed if needs be
	private List<Node> children = new ArrayList<>();
	private double probability = 0.0; //only a field for A1-nodes
	
	//constructor for root node
	public Node(State state) {
		this.parentNode = null;
		this.prevAction = null;
		this.nodeState = state;
		this.depth = 0;
		this.totalScore = 0;
		this.numberOfTimesVisited = 0;
		this.ucbValue = bigValue;
		this.depth = 0;
		this.totalScore = 0;
	}
	
	//constructor for treeNode
	public Node(Node parent, Action prevAction, State state) {
		this.parentNode = parent;
		this.prevAction = prevAction;
		this.nodeState = state;
		this.depth = parent.depth+1;
		this.totalScore = 0;
		this.numberOfTimesVisited = 0;
		this.ucbValue = bigValue;
		parent.children.add(this);
		
	}
	//constructor for treeNode that has prevAction A1
	public Node(Node parent, State state, double probability) {
		this.probability = probability; //probability for ending up in this node after taken an A1 action in the parent node
		this.parentNode = parent;
		this.nodeState = state;
		this.depth = parent.depth+1;
		this.totalScore = 0;
		this.numberOfTimesVisited = 0;
		this.ucbValue = bigValue;
		parent.children.add(this);
		
	}
	
	
	//constructor for simulated node
	public Node(Node supernode, State state) {
		this(supernode, null, state);
		this.children = null;
	}
	
	//methods
	

	
	public void calculateUcbScore() {
		if (numberOfTimesVisited == 0) {
			this.ucbValue = bigValue;
		}
		this.ucbValue = totalScore/numberOfTimesVisited + Math.sqrt(explConstant*Math.log(parentNode.getNumberOfTimesVisited())/totalScore);
	}

	//getters and setters
	
	public double getUcbValue() {
		return ucbValue;
	}

	public void setUcbValue(double ucbValue) {
		this.ucbValue = ucbValue;
	}

	public int getNumberOfTimesVisited() {
		return numberOfTimesVisited;
	}

	public void setNumberOfTimesVisited(int numberOfTimesVisited) {
		this.numberOfTimesVisited = numberOfTimesVisited;
	}

	public void setTotalScore(double totalScore) {
		this.totalScore = totalScore;
	}
	
	public State getNodeState() {
		return nodeState;
	}

	public void setNodeState(State nodeState) {
		this.nodeState = nodeState;
	}

	public Action getPrevAction() {
		return prevAction;
	}

	public Node getParentNode() {
		return parentNode;
	}

	public int getDepth() {
		return depth;
	}
	
	public double getProbability() {
		return probability;
	}

	public double getTotalScore() {
		return totalScore;
	}


	public boolean isTerminalState() {
		return isTerminalState;
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
	public void addChild(Node node) {
		children.add(node);
	}
	
	public String toString() {
		String result = "(Node type: ";
		if (this.probability == 0.0) {
			if (this.parentNode == null) {
				result += "root | ";
			} else {
				result += "A2-A6 | ";
			}
		} else {
			result += "A1 | p: " + probability + " | ";
		}
		result += "d: " + depth + " | s: " + totalScore + " | UCB: " + ucbValue + " | n: " + numberOfTimesVisited +")";
		result += "\n with " + getNodeState();
		return result;
	}
	
	//Main for testing

}
