package solver;
import simulator.*;
import problem.*;
 
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
 
public class Solver {
   
   
    //fields
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
    public Solver(ProblemSpec ps, String outputFileName) {
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
        this.discountFactor = ps.getDiscountFactor();
        this.goalIndex = ps.getN();
        this.maxNumberOfTimeSteps = ps.getMaxT();
        this.currentState = State.getStartState(ps.getFirstCarType(), ps.getFirstDriver(), ps.getFirstTireModel());
        run();
       
    }
   
   
    //methods
    private void run() {
       
    }
 
 
   
    //main for testing
    public static void main(String[] args) {
       
 
    }
 
}