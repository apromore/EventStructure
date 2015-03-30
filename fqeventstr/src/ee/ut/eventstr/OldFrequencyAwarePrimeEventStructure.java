package ee.ut.eventstr;

import java.util.List;
import java.util.Map;

public class OldFrequencyAwarePrimeEventStructure<T> extends OldPrimeEventStructure<T> {
	private Map<Integer, Integer> occurrences;
	private double[][] fmatrix;

	public OldFrequencyAwarePrimeEventStructure(BehaviorRelation[][] matrix, boolean[][] dcausality,
			Map<T, Integer> map, List<T> ridx, List<String> labels, String modelName, Map<Integer, Integer> occurrences, double[][] fmatrix) {
		super(matrix, dcausality, map, ridx, labels, modelName);
		this.occurrences = occurrences;
		this.fmatrix = fmatrix;
	}
	
	public double[][] getFreqMatrix() {
		return fmatrix;
	}
	
	public Map<Integer, Integer> getOccurrences() {
		return occurrences;
	}
}