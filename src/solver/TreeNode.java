package solver;
import problem.*;
import simulator.*;

public class TreeNode {
	
	//fields
	private Action prevAction;
	private TreeNode parentNode;
	private int depth;
	private double totalScore; //accumalated total score
	private double ucbValue;
	private int numberOfTimesVisited; //how many times have we visited this node. Only interesting for the root's children
	private State nodeState;
	private boolean isLeafNode;
	private boolean isTerminalState;
	private final int explConstant = 2; //This constant can be changed if needs be
	
	//constructor
	public TreeNode(TreeNode parent, Action prevAction, State state) {
		this.parentNode = parent;
		this.prevAction = prevAction;
		this.nodeState = state;
		this.depth = parent.depth+1;
		this.totalScore = 0;
		this.numberOfTimesVisited = 0;
	}
	
	//constructor for root node
	public TreeNode(State state) {
		this(null, null, state);
		this.depth = 0;
		this.totalScore = 0;
		
	}
	
	//methods
	

	
	public void calculateUcbScore() {
		this.ucbValue = totalScore/numberOfTimesVisited + Math.sqrt(explConstant*Math.log(parentNode.getNumberOfTimesVisited())/totalScore);
	}

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

	public TreeNode getParentNode() {
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
	
	//getters

	
	//Main for testing

}
