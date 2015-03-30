//package ee.ut.eventstr.comparison.test;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//
//import org.jbpt.utils.IOUtils;
//import org.junit.Test;
//import org.processmining.framework.util.Pair;
//
//import com.google.common.collect.ImmutableMap;
//
//import ee.ut.eventstr.BehaviorRelation;
//import ee.ut.eventstr.OldPrimeEventStructure;
//import ee.ut.eventstr.comparison.PESSemantics;
//import ee.ut.eventstr.comparison.PSPDifferenceVerbalizer;
//import ee.ut.eventstr.comparison.PartialSynchronizedProduct;
//
//public class PSPTest {
//
//	@Test
//	public void test() {
//		OldPrimeEventStructure<Integer> pes1, pes2;
//		PartialSynchronizedProduct<Integer> psp = 
//				new PartialSynchronizedProduct<>(new PESSemantics<Integer>(pes1 = getPES1()), new PESSemantics<Integer>(pes2 = getPES2()));
//		psp.perform();//.prune();
//		IOUtils.toFile("target/psp.dot", psp.toDot());
//	    List<Object> dots = new PSPDifferenceVerbalizer<Integer>(pes1, pes2).getDifferences(psp.getDifferences());
//	    
//	    for (int i = 0; i < dots.size(); i++) {
//	    	Object o= dots.get(i);
//	    	if (o instanceof Pair<?,?>) {
//	    		Pair<String, String> pair = (Pair<String,String>)o;
//	    		IOUtils.toFile(String.format("target/porun%d_1.dot", i), pair.getFirst());
//	    		IOUtils.toFile(String.format("target/porun%d_2.dot", i), pair.getSecond());
//	    	}
//	    }
//
//	}
//
//	public OldPrimeEventStructure<Integer> getPES1() {
//		BehaviorRelation[][] matrix = {
//				{BehaviorRelation.CONCURRENCY, BehaviorRelation.CAUSALITY, BehaviorRelation.CAUSALITY},
//				{BehaviorRelation.INV_CAUSALITY, BehaviorRelation.CONCURRENCY, BehaviorRelation.CONCURRENCY},
//				{BehaviorRelation.INV_CAUSALITY, BehaviorRelation.CONCURRENCY, BehaviorRelation.CONCURRENCY}
//			};
//		boolean [][] dcausality = {{false, true, true},{false, false, false},{false, false, false}};
//		
//		Map<Integer, Integer> map = ImmutableMap.of(0,0,1,1,2,2);
//		List<Integer> ridx = Arrays.asList(0,1,2,3);
//		List<String> labels = Arrays.asList("a","b","c");
//		return new OldPrimeEventStructure<Integer>(matrix, dcausality, map, ridx, labels, "PES1");
//	}
//	public OldPrimeEventStructure<Integer> getPES2() {
//		BehaviorRelation[][] matrix = {
//				{BehaviorRelation.CONCURRENCY, BehaviorRelation.CAUSALITY, BehaviorRelation.CAUSALITY},
//				{BehaviorRelation.INV_CAUSALITY, BehaviorRelation.CONCURRENCY, BehaviorRelation.CONFLICT},
//				{BehaviorRelation.INV_CAUSALITY, BehaviorRelation.CONFLICT, BehaviorRelation.CONCURRENCY}
//			};
//		boolean [][] dcausality = {{false, true, true},{false, false, false},{false, false, false}};
//		
//		Map<Integer, Integer> map = ImmutableMap.of(0,0,1,1,2,2);
//		List<Integer> ridx = Arrays.asList(0,1,2,3);
//		List<String> labels = Arrays.asList("a","b","c");
//		return new OldPrimeEventStructure<Integer>(matrix, dcausality, map, ridx, labels, "PES1");
//	}
//}
