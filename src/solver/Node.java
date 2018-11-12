package solver;
import java.util.ArrayList;
import java.util.List;

import problem.Action;
import simulator.State;

public class Node{
	
	private final static int bigValue = 100000;
	
	//fields
	private Action prevAction;
	private Node parentNode;
	private int depth;
	private double totalScore; //accumalated total score
	private double ucbValue;
	private int numberOfTimesVisited; //how many times have we visited this node. Only interesting for the root's children
	private State nodeState;
	private final int explConstant = 2; //This constant can be changed if needs be
	private List<Node> children = new ArrayList<>();
	protected int timeStep;
	private Node superNode; //Only interesting for simulated node in rollout. Supernode is the leaf node that this Node is rollouted from
	
	//constructor for root node
	public Node(State state, int rootTimeStep) {
		this.parentNode = null;
		this.prevAction = null;
		this.nodeState = state;
		this.depth = 0;
		this.totalScore = 0;
		this.numberOfTimesVisited = 0;
		this.timeStep = rootTimeStep;
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
		this.timeStep = parent.getTimeStep() +1;
		if(state.isInBreakdownCondition()) {
			this.timeStep += Solver.repairTime -1;
		}
		else if(state.isInSlipCondition()) {
			this.timeStep += Solver.slipRecoveryTime -1;
		}
		this.ucbValue = calculateUcbScore();
		
	}

	
	//constructor for simulated node. IS THIS EVEN USED?
	public Node(Node superNode, State state) {
		this(superNode, null, state);
		System.out.println("HEI");
		this.children = null;
		System.out.println(this.timeStep);
	}
	
	//methods
	

	
	public double calculateUcbScore() {
		if (numberOfTimesVisited == 0) {
			return bigValue;
		}
		if(this.parentNode==null) {
			System.out.println("UPDATING UCB FOR ROOTNODE");
		}
		double a = totalScore/numberOfTimesVisited + Math.sqrt(explConstant*Math.log(parentNode.getNumberOfTimesVisited())/totalScore);
		setUcbValue(a);
		return a;
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
	
	public Node getSuperNode() {
		return this.superNode;
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

	
	public List<Node> getChildren() {
		return children;
	}
	
	//add child
	public void addChild(Node node) {
		children.add(node);
	}
	
	public String toString() {
		String result = timeStep + " (Node type: ";

		if(this.parentNode == null) {
			result += "root | ";
			}
		else{
			result += "A2-A6 | ";
		}

		result += "d: " + depth + " | s: " + totalScore + " | UCB: " + ucbValue + " | n: " + numberOfTimesVisited +")";
		result += "\n with " + getNodeState();
		return result;
	}

	
	//getTimeStep
	public int getTimeStep() {
		return timeStep;
	}


	
	//Main for testing

}
