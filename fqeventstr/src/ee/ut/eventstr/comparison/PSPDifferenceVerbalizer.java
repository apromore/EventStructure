package ee.ut.eventstr.comparison;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.processmining.framework.util.Pair;

import ee.ut.eventstr.PrimeEventStructure;
import ee.ut.eventstr.comparison.PartialSynchronizedProduct.Operation;
import ee.ut.eventstr.comparison.PartialSynchronizedProduct.Operation.Op;
import ee.ut.eventstr.comparison.PartialSynchronizedProduct.State;

public class PSPDifferenceVerbalizer <T> {
	private PrimeEventStructure<T> pes1;
	private PrimeEventStructure<T> pes2;

	public PSPDifferenceVerbalizer(PrimeEventStructure<T> pes1, PrimeEventStructure<T> pes2) {
		this.pes1 = pes1;
		this.pes2 = pes2;
	}
	
	public List<Object> getDifferences(Map<State, List<List<Operation>>> diffs) {
		List<Object> result = new ArrayList<>();
		
		for (Entry<State, List<List<Operation>>> entry: diffs.entrySet()) {
			BitSet lhidings = new BitSet();
			BitSet rhidings = new BitSet();
			
			for (List<Operation> block: entry.getValue())
				for (Operation op: block) 
					if (op.op == Op.LHIDE)
						lhidings.set((Integer)op.target);
					else if (op.op == Op.RHIDE)
						rhidings.set((Integer)op.target);
			
			String dotPes1 = pes1.toDot(entry.getKey().c1, lhidings);
			String dotPes2 = pes2.toDot(entry.getKey().c2, rhidings);
			
			result.add(new Pair<String, String>(dotPes1, dotPes2));			
		}
		return result;
	}
}
