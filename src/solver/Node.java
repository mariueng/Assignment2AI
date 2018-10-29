package solver;
import java.util.ArrayList;
import java.util.List;

import problem.Action;
import simulator.State;

public class Node {
	
	//fields
	private Action prevAction;
	private Node parentNode;
	private int depth;
	private double totalScore; //accumalated total score
	private double ucbValue;
	private int numberOfTimesVisited; //how many times have we visited this node. Only interesting for the root's children
	private State nodeState;
	private boolean isLeafNode;
	private boolean isTerminalState;
	private final int explConstant = 2; //This constant can be changed if needs be
	private List<Node> children;
	
	//constructor
	public Node(Node parent, Action prevAction, State state) {
		this.parentNode = parent;
		this.prevAction = prevAction;
		this.nodeState = state;
		this.depth = parent.depth+1;
		this.totalScore = 0;
		this.numberOfTimesVisited = 0;
		this.children = new ArrayList<Node>();
		parent.children.add(this);
		
	}
	
	//constructor for root node
	public Node(State state) {
		this(null, null, state);
		this.depth = 0;
		this.totalScore = 0;
		
	}
	
	//constructor for simulated nodes
	public Node(Node supernode, State state) {
		this(supernode, null, state);
		this.children = null;
	}
	
	//methods
	

	
	public void calculateUcbScore() {
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

	public double getTotalScore() {
		return totalScore;
	}

	public boolean isLeafNode() {
		return isLeafNode;
	}

	public boolean isTerminalState() {
		return isTerminalState;
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
	//Main for testing

}
