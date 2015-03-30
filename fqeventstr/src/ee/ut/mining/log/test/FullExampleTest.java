package ee.ut.mining.log.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Test;

import ee.ut.eventstr.OldFrequencyAwarePrimeEventStructure;
import ee.ut.eventstr.OldPrimeEventStructure;
import ee.ut.eventstr.util.PORunsToPES;
import ee.ut.mining.log.AbstractingShortLoopsPORun;
import ee.ut.mining.log.AlphaRelations;
import ee.ut.mining.log.PORun;
import ee.ut.mining.log.PORuns;
import ee.ut.mining.log.XLogReader;

public class FullExampleTest {
	String fileName_trace  = "./mxml/SimulationTest/%s.mxml";
	
	public OldFrequencyAwarePrimeEventStructure<Integer> buildFRAPES(String model) throws Exception {
		XLog log = XLogReader.openLog(String.format(fileName_trace, model));		
		AlphaRelations alphaRelations = new AlphaRelations(log);
		
		File target = new File("target");
		if (!target.exists())
			target.mkdirs();
		
		PORuns runs = new PORuns();

		for (XTrace trace: log) {
			PORun porun = 
//					new PORun(alphaRelations, trace);
					new AbstractingShortLoopsPORun(alphaRelations, trace);
			runs.add(porun);
		}
		
		runs.mergePrefix();
		
		return PORunsToPES.getFrequencyEnhancedPrimeEventStructure(runs, model);
	}
	
	public void printFes(OldFrequencyAwarePrimeEventStructure<Integer> pes) {
		String str = "";
		for (String s: pes.getLabels()) {
			str += s + " ";
		}
		str += "\n";
				
		for (int i = 0; i < pes.getFreqMatrix().length; i++) {
			for (int j = 0; j < pes.getFreqMatrix().length; j++) {
				str += pes.getFreqMatrix()[i][j] + " ";
			}
			str += "\n";
		}	
		System.out.println(str);
	}

	@Test
	public void test() throws Exception {
	    long time = System.nanoTime();

	    //FrequencyAwarePrimeEventStructure<Integer> pes1 = buildFRAPES("Order2Cash1_5000");
	    //FrequencyAwarePrimeEventStructure<Integer> pes2 = buildFRAPES("Order2Cash2_5000");
	    OldFrequencyAwarePrimeEventStructure<Integer> pes1 = buildFRAPES("normallog");
	    OldFrequencyAwarePrimeEventStructure<Integer> pes2 = buildFRAPES("deviantlog");
	        
	    printFes(pes1);
	    
	    // ==============================================================
	    // TODO:  Complete the porting of the following code.
	    // ==============================================================
//	    
//	    PartialSynchronizedProduct<Integer> psp = 
//	    		new PartialSynchronizedProduct<>(new PESSemantics<>(pes1), new PESSemantics<>(pes2));
//	    
//	    psp.perform();
////	    	.prune();
//		IOUtils.toFile("target/psp2.dot", psp.toDot());
//
//	    List<Object> dots = new PSPDifferenceVerbalizer<Integer>(pes1, pes2).getDifferences(psp.getDifferences());
//	    
//	    for (int i = 0; i < dots.size(); i++) {
//	    	Object o= dots.get(i);
//	    	if (o instanceof Pair<?,?>) {
//	    		Pair<String, String> pair = (Pair<String,String>)o;
//	    		IOUtils.toFile(String.format("target/porun_%d_1.dot", i), pair.getFirst());
//	    		IOUtils.toFile(String.format("target/porun_%d_2.dot", i), pair.getSecond());
//	    	}
//	    }
//	    
	    System.out.println("Overall time: " + (System.nanoTime() - time) / 1000000000.0);	    
	}

	public void dumpPES(String model, OldPrimeEventStructure<Integer> pes)
			throws FileNotFoundException {
		System.out.println("Done with pes");
	    PrintStream out = null;
	    out = new PrintStream("target/" + model+".pes.tex");
//	    pes.toLatex(out);
	    out.close();
	    System.out.println("Done with toLatex");
	}
}
