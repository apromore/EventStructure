package ee.ut.mining.log.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.utils.IOUtils;
import org.junit.Test;

import ee.ut.eventstr.OldPrimeEventStructure;
import ee.ut.eventstr.util.PORuns2PES;
import ee.ut.mining.log.AbstractingShortLoopsPORun;
import ee.ut.mining.log.AlphaRelations;
import ee.ut.mining.log.PORun;
import ee.ut.mining.log.PORuns;
import ee.ut.mining.log.XLogReader;

public class SmallExampleTest {
	String model = "z_with_skip";
	String fileName_trace  = "./mxml/%s.mxml.gz";

	@Test
	public void test() throws Exception {
		XLog log = XLogReader.openLog(String.format(fileName_trace, model));		
		AlphaRelations alphaRelations = new AlphaRelations(log);
		
		File target = new File("target");
		if (!target.exists())
			target.mkdirs();
		
	    long time = System.nanoTime();
		PORuns runs = new PORuns();

		for (XTrace trace: log) {
			PORun porun = 
//					new PORun(alphaRelations, trace);
					new AbstractingShortLoopsPORun(alphaRelations, trace);
			runs.add(porun);
		}
		
		IOUtils.toFile("target/" + model + "_prefix.dot", runs.toDot());
		runs.mergePrefix();
		IOUtils.toFile("target/" + model + "_merged.dot", runs.toDot());

		PORuns2PES.getPrimeEventStructure(runs, model);
//		FrequencyAwarePrimeEventStructure<Integer> pes = PORunsToPES.getFrequencyEnhancedPrimeEventStructure(runs, model);
//		dumpPES(model, pes);
		
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
