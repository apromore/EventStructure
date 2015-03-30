package ee.ut.eventstr;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PESSemantics <T> {
	private PrimeEventStructure<T> pes;
	
	public PESSemantics(PrimeEventStructure<T> pes) {
		this.pes = pes;
	}
	
	Set<BitSet> maxConf;
	public Set<BitSet> getMaxConf() {
		if (maxConf == null) {
			maxConf = new HashSet<>();
			BitSet _sinks = new BitSet();
			for (Integer s1: pes.sinks)
				_sinks.set(s1);
			
			while (!_sinks.isEmpty()) {
				int pivot = _sinks.nextSetBit(0);
				
				// Let's compute the CUT associated with "pivot" (CUT is a maximal co-set)
				BitSet cut = (BitSet) pes.concurrency[pivot].clone();
				cut.and(_sinks);
				cut.set(pivot);
				
				_sinks.xor(cut);
				
				BitSet conf = new BitSet();
				for (int sink = cut.nextSetBit(0); sink >= 0; sink = cut.nextSetBit(sink+1)) {
					conf.or(pes.invcausality[sink]);
					conf.set(sink);
				}
				
				maxConf.add(conf);
			}
		}
		return maxConf;
	}

	public List<String> getLabels() {
		return pes.labels;
	}
	
	public String getLabel(int e1) {
		return pes.labels.get(e1);
	}

	public BitSet getPossibleExtensions(BitSet conf) {
		if (conf.isEmpty()) {
			BitSet result = new BitSet();
			for (Integer src: pes.sources)
				result.set(src);
			return result;
		}
		BitSet conflicting = new BitSet();
		BitSet concurrent = new BitSet();
		BitSet dcausal = new BitSet();
		
		for (int e = conf.nextSetBit(0); e >= 0; e = conf.nextSetBit(e+1)) {
			conflicting.or(pes.conflict[e]);
			concurrent.or(pes.concurrency[e]);
			dcausal.or(pes.dcausality[e]);
		}

		dcausal.or(concurrent);
		dcausal.andNot(conf);
		dcausal.andNot(conflicting);
		
		BitSet possibleExtensions = new BitSet();
		for (int e = dcausal.nextSetBit(0); e >= 0; e = dcausal.nextSetBit(e+1)) {
			BitSet union = (BitSet)conf.clone();
			union.or(pes.invcausality[e]);
			if (union.cardinality() == conf.cardinality())
				possibleExtensions.set(e);
		}
		
		return possibleExtensions;
	}
	
	public BitSet getPossibleFuture(BitSet conf) {
		BitSet conflicting = new BitSet();
		
		for (int e = conf.nextSetBit(0); e >= 0; e = conf.nextSetBit(e+1))
			conflicting.or(pes.conflict[e]);

		conflicting.flip(0, pes.labels.size());
		conflicting.andNot(conf);
		
		return conflicting;
	}
	
	public Set<String> getPossibleFutureAsLabels(BitSet conf) {
		BitSet future = getPossibleFuture(conf);
		
		Set<String> flabels = new HashSet<>();
		for (int e = future.nextSetBit(0); e >= 0; e = future.nextSetBit(e+1))
			flabels.add(pes.labels.get(e));
		
		return flabels;
	}

	public BehaviorRelation getBRelation(int e1, int e2) {
		return pes.getBRelMatrix()[e1][e2];
	}

	public BitSet getDirectPredecessors(int e) {
		BitSet pred = new BitSet();
		for (int i = 0; i < pes.labels.size(); i++)
			if (pes.dcausality[i].get(e))
				pred.set(i);
		return pred;
	}	
	
	public BitSet getLocalConfiguration(int e) {
		BitSet conf = (BitSet)pes.invcausality[e].clone();
		conf.set(e);
		return conf;
	}
}
