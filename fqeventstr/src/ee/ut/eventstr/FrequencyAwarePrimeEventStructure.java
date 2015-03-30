package ee.ut.eventstr;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class FrequencyAwarePrimeEventStructure <T> extends PrimeEventStructure<T> {

	private Map<Integer, Integer> occurrences;
	private double[][] fmatrix;

	public FrequencyAwarePrimeEventStructure(List<String> labels, BitSet[] causality, BitSet[] dcausality,
			BitSet[] invcausality, BitSet[] concurrency, BitSet[] conflict, List<Integer> sources, List<Integer> sinks,
			Map<Integer, Integer> occurrences, double[][] fmatrix) {
		super(labels, causality, dcausality, invcausality, concurrency, conflict, sources, sinks);
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
