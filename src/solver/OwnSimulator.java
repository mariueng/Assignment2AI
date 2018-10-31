package solver;
import problem.*;
import simulator.*;

public class OwnSimulator {
	
	/**
	 * This class will have input: node (superNode)
	 * Method: getNextA1State(Node node, int k) and output State
	 * Method: getNextState(Node node, Action action) and output State 
	 * When action is A1 (Move), it takes probabilities into account
	 */
	
	//fields
	private ProblemSpec ps;
	
	
	//constructor
	public OwnSimulator(ProblemSpec ps){
		this.ps = ps;
	}
	
	//methods
	

	
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
	 * Method for reting timestep number
	 * @return
	 */
	public int getTimeStepNumber() {
		int result = 0;
		return result;
	}
	
	

	
	//Main for testing
	
}
