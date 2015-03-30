package ee.ut.eventstr.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jbpt.utils.IOUtils;
import org.processmining.framework.util.Pair;

import com.google.common.collect.Multimap;

import ee.ut.eventstr.FrequencyAwarePrimeEventStructure;
import ee.ut.eventstr.PrimeEventStructure;
import ee.ut.graph.transitivity.BitsetDAGAlgorithms;
import ee.ut.mining.log.PORuns;

public class PORuns2PES {
	public static PrimeEventStructure<Integer> getPrimeEventStructure(PORuns runs, String modelName) {
		return getPrimeEventStructure(runs.getSuccessors(), runs.getConcurrency(), runs.getSources(),
				runs.getSinks(), runs.getLabels(), modelName);
	}
	public static PrimeEventStructure<Integer> getPrimeEventStructure(
			Multimap<Integer, Integer> adj, Multimap<Integer, Integer> conc,
			List<Integer> sources, List<Integer> sinks,
			Map<Integer,String> lmap, String modelName) {
		int size = lmap.size();
		Pair<BitSet[], BitSet[]> pair
				= BitsetDAGAlgorithms.transitivityDAG(adj, size, sources);
		BitSet[] causality = pair.getFirst();
		BitSet[] dcausality = pair.getSecond();

		BitSet[] invcausality = new BitSet[size];
		BitSet[] concurrency = new BitSet[size];
		BitSet[] conflict = new BitSet[size];
		List<String> labels = new ArrayList<>(size);
		
		for (int i = 0; i < size; i++) {
			invcausality[i] = new BitSet();
			concurrency[i] = new BitSet();
			conflict[i] = new BitSet();
			labels.add(lmap.get(i));
		}
		
		for (Entry<Integer, Integer> entry: conc.entries())
			concurrency[entry.getKey()].set(entry.getValue());
		
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				if (causality[i].get(j))
					invcausality[j].set(i);
		
		for (int i = 0; i < size; i++) {
			BitSet union = (BitSet) causality[i].clone();
			union.or(invcausality[i]);
			union.or(concurrency[i]);
			union.set(i); // Remove IDENTITY
			conflict[i].flip(0, size);
			conflict[i].xor(union);
		}
		
		PrimeEventStructure<Integer> pes = 
				new PrimeEventStructure<Integer>(labels, causality, dcausality, invcausality,
						concurrency, conflict, sources, sinks);
		
		IOUtils.toFile("target/pes.dot", pes.toDot());
		return pes;
	}	
	
	public static FrequencyAwarePrimeEventStructure<Integer> getFrequencyAwarePrimeEventStructure(PORuns runs, String modelName) {
		return getFrequencyAwarePrimeEventStructure(runs.getSuccessors(), runs.getConcurrency(), runs.getSources(),
				runs.getSinks(), runs.getLabels(), runs.getEquivalenceClasses(), modelName);
	}
	public static FrequencyAwarePrimeEventStructure<Integer> getFrequencyAwarePrimeEventStructure(
			Multimap<Integer, Integer> adj, Multimap<Integer, Integer> conc,
			List<Integer> sources, List<Integer> sinks,
			Map<Integer,String> lmap, Multimap<Integer, Integer> equivalenceClasses, String modelName) {
		int size = lmap.size();
		Pair<BitSet[], BitSet[]> pair
				= BitsetDAGAlgorithms.transitivityDAG(adj, size, sources);
		BitSet[] causality = pair.getFirst();
		BitSet[] dcausality = pair.getSecond();

		BitSet[] invcausality = new BitSet[size];
		BitSet[] concurrency = new BitSet[size];
		BitSet[] conflict = new BitSet[size];
		List<String> labels = new ArrayList<>(size);
		
		for (int i = 0; i < size; i++) {
			invcausality[i] = new BitSet();
			concurrency[i] = new BitSet();
			conflict[i] = new BitSet();
			labels.add(lmap.get(i));
		}
		
		for (Entry<Integer, Integer> entry: conc.entries())
			concurrency[entry.getKey()].set(entry.getValue());
		
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				if (causality[i].get(j))
					invcausality[j].set(i);
		
		for (int i = 0; i < size; i++) {
			BitSet union = (BitSet) causality[i].clone();
			union.or(invcausality[i]);
			union.or(concurrency[i]);
			union.set(i); // Remove IDENTITY
			conflict[i].flip(0, size);
			conflict[i].xor(union);
		}
		
		Map<Integer, Integer> occurrences = new HashMap<Integer, Integer>();
		for (Integer event: equivalenceClasses.keySet())
			occurrences.put(event, equivalenceClasses.get(event).size());

		double[][] fmatrix = new double[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (dcausality[i].get(j))
					fmatrix[i][j] = (occurrences.get(j) + 0.0f) / occurrences.get(i);
			}
		}

		
		FrequencyAwarePrimeEventStructure<Integer> pes = 
				new FrequencyAwarePrimeEventStructure<Integer>(labels, causality, dcausality, invcausality,
						concurrency, conflict, sources, sinks, occurrences, fmatrix);
		
		IOUtils.toFile("target/fapes.dot", pes.toDot());
		return pes;
	}	
}
