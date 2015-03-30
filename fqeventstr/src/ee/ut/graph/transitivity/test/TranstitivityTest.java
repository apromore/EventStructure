package ee.ut.graph.transitivity.test;

import java.util.BitSet;
import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import ee.ut.graph.transitivity.BitsetDAGAlgorithms;

public class TranstitivityTest {

	@Test
	public void testTransitiveClosure() {
		Multimap<Integer, Integer> adj = HashMultimap.create();
		
		for (int i = 0; i < 4; i++)
			adj.put(i, i + 1);
		
		System.out.println("Transitive closure");
		for (BitSet row: BitsetDAGAlgorithms.transitiveClosureDAG(adj, 5, Collections.singleton(0)))
			System.out.println(row);
	}
	
	@Test
	public void testTransitiveReduction() {
		Multimap<Integer, Integer> adj = HashMultimap.create();
		
		for (int i = 0; i < 5; i++)
			for (int j = i + 1; j < 5; j++)
				adj.put(i, j);
		
		System.out.println("Transitive reduction");
		for (BitSet row: BitsetDAGAlgorithms.transitiveReductionDAG(adj, 5, Collections.singleton(0)))
			System.out.println(row);
	}
}
