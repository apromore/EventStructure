package ee.ut.eventstr.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Multimap;

import ee.ut.eventstr.BehaviorRelation;
import ee.ut.eventstr.OldFrequencyAwarePrimeEventStructure;
import ee.ut.eventstr.OldPrimeEventStructure;
import ee.ut.graph.util.GraphUtils;
import ee.ut.mining.log.PORuns;

public class PORunsToPES {
	static class Processor {
		private HashMap<Integer, Integer> pesMap;
		private List<Integer> pesRIdx;
		private List<String> pesLabels;
		private BehaviorRelation[][] matrix;
		private boolean[][] dcausality;
		private PORuns runs;
		
		public Processor initializePES(PORuns runs) {
			this.runs = runs;
			pesMap = new HashMap<>();
			pesRIdx = new ArrayList<>();
			pesLabels = new ArrayList<>();
			
			Multimap<Integer, Integer> successors = runs.getSuccessors();
			int index = 0;
			for (Entry<Integer, String> entry: runs.getLabels().entrySet()) {
				pesMap.put(entry.getKey(), index++);
				pesRIdx.add(entry.getKey());
				pesLabels.add(entry.getValue());
			}
			
//			System.out.println("Step 1");
			int size = pesLabels.size();
			
			matrix = new BehaviorRelation[size][size];
			boolean[][] causality = new boolean[size][size];
			dcausality = new boolean[size][size];

			for (int i = 0; i < size; i++) {
				Arrays.fill(matrix[i], BehaviorRelation.CONFLICT);
				matrix[i][i] = BehaviorRelation.CONCURRENCY;
			}

//			System.out.println("Step 2; size: " + size);

			for (Integer _src: successors.keySet()) {
				Integer src = pesMap.get(_src);
				for (Integer _tgt: successors.get(_src))
					//// TODO: Update this !!!!
					//// <<<<<<< I am assuming that the Partially Ordered Runs are transitively reduced !!!!!
					causality[src][pesMap.get(_tgt)] = dcausality[src][pesMap.get(_tgt)] = true;	
			}
//			System.out.println("Step 3");

			for (Entry<Integer, Integer> entry: runs.getConcurrency().entries()) {
				Integer ev1 = pesMap.get(entry.getKey());
				Integer ev2 = pesMap.get(entry.getValue());
				if (ev1 != null && ev2 != null)
					matrix[ev1][ev2] = BehaviorRelation.CONCURRENCY;
			}
			
//			System.out.println("Step 4");

			GraphUtils.transitiveClosure(causality);
			
//			System.out.println("Step 5 (Transitive closure)");

			for (int i = 0; i < size; i++)
				for (int j = 0; j < size; j++)
					if (causality[i][j]) {
						matrix[i][j] = BehaviorRelation.CAUSALITY;
						matrix[j][i] = BehaviorRelation.INV_CAUSALITY;
					}		
//			System.out.println("Step 6");
			
			return this;
		}
		
		public OldPrimeEventStructure<Integer> getPrimeEventStructure(String modelName) {
			return new OldPrimeEventStructure<>(matrix, dcausality, pesMap, pesRIdx, pesLabels, modelName);			
		}
		
		public OldFrequencyAwarePrimeEventStructure<Integer> getFrequencyEnhancedPrimeEventStructure(String modelName) {
			int size = pesLabels.size();

			Multimap<Integer, Integer> equivalenceClasses = runs.getEquivalenceClasses();
			Map<Integer, Integer> occurrences = new HashMap<Integer, Integer>();
			for (Integer event: equivalenceClasses.keySet())
				occurrences.put(event, equivalenceClasses.get(event).size());

			double[][] fmatrix = new double[size][size];
			for (int i = 0; i < size; i++) {
				int ip = pesRIdx.get(i);
				for (int j = 0; j < size; j++) {
					int jp = pesRIdx.get(j);
					switch (matrix[i][j]) {
					case CAUSALITY:
						fmatrix[i][j] = occurrences.get(jp) / occurrences.get(ip);
						break;
					case CONCURRENCY:
						fmatrix[i][j] = 1.0;
					default: // Nothing to be done, "fmatrix" is fully initialized with 0.0
					}
				}
			}
				
			return new OldFrequencyAwarePrimeEventStructure<>(matrix, dcausality, pesMap, pesRIdx, pesLabels, modelName, occurrences, fmatrix);
		}
	}
	
	public static OldPrimeEventStructure<Integer> getPrimeEventStructure(PORuns runs, String modelName) {
		return new Processor().initializePES(runs).getPrimeEventStructure(modelName);
	}
	
	public static OldFrequencyAwarePrimeEventStructure<Integer> getFrequencyEnhancedPrimeEventStructure(PORuns runs, String modelName) {
		return new Processor().initializePES(runs).getFrequencyEnhancedPrimeEventStructure(modelName);
	}
}
