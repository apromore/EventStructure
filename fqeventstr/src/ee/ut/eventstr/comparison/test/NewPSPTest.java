package ee.ut.eventstr.comparison.test;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.utils.IOUtils;
import org.junit.Test;

import utilities.FES.ComparatorFreq;
import utilities.FES.FreqEventStructure;
import ee.ut.eventstr.FrequencyAwarePrimeEventStructure;
import ee.ut.eventstr.PESSemantics;
import ee.ut.eventstr.PrimeEventStructure;
import ee.ut.eventstr.comparison.PartialSynchronizedProduct;
import ee.ut.eventstr.util.PORuns2PES;
import ee.ut.mining.log.AlphaRelations;
import ee.ut.mining.log.PORun;
import ee.ut.mining.log.PORuns;
import ee.ut.mining.log.XLogReader;

public class NewPSPTest {
	String fileName_trace  = 
//			"./mxml/iteration1/%s.xes.gz"
//			"./mxml/simulationtest/%s.mxml"
//			"./mxml/%s.mxml"
			"./mxml/exp/%s.mxml"
//			"./xes/loan/%s.xes"
			;
	
	String model1 = 
//			"normallog"
//			"b1_no_loops2"
//			"base"
//			"base_no_loops"
//			"conditional_no_loops"
			"hospitalno"
			;

	String model2 =
//			"deviantlog"
//			"c1_no_loops2"
//			"r1_no_loops2"
//			"cd1_no_loops"
//			"s1_no_loop2"
//			"replace1"
//			"rp1_no_loops2"
//			"parallel_serial_no_loops"
//			"copy"
//			"conditional_no_loops"
//			"replace_no_loops"
//			"swap_no_loops"
//			"sync_no_loops"
//			"IOR_no_loops"
			"hospitalyes"
//			"ROI_no_loops"
			;
	
	@Test
	public void testCode() throws Exception {
		double t1 = System.currentTimeMillis();
		PORuns runs1 = getRuns(model1);
		PORuns runs2 = getRuns(model2);
		
		double t2 = System.currentTimeMillis();
		PrimeEventStructure<Integer> pes1 = PORuns2PES.getPrimeEventStructure(runs1, model1);
		double t3 = System.currentTimeMillis();
		PrimeEventStructure<Integer> pes2 = PORuns2PES.getPrimeEventStructure(runs2, model2);
		double t4 = System.currentTimeMillis();
		FrequencyAwarePrimeEventStructure<Integer> fpes1 = PORuns2PES.getFrequencyAwarePrimeEventStructure(runs1, model1);
		double t5 = System.currentTimeMillis();
		FrequencyAwarePrimeEventStructure<Integer> fpes2 = PORuns2PES.getFrequencyAwarePrimeEventStructure(runs2, model2);	
		double t6 = System.currentTimeMillis();
		
		PartialSynchronizedProduct<Integer> psp = 
				new PartialSynchronizedProduct<>(new PESSemantics<>(pes1), new PESSemantics<>(pes2));
		
		//remove duplicates
		Set<String> diffset = new HashSet<String>(psp.perform()
				.prune()
				.getDiff());
		
		//IOUtils.toFile(String.format("target/%s_X_%s_psp.dot", model1, model2), psp.toDot());		

		psp.shortestPathDijkstra();
		
		for (String diff: diffset) {
			System.out.println("DIFF: " + diff);
		}
		double t7 = System.currentTimeMillis();
				
		FreqEventStructure fes1 = new FreqEventStructure(fpes1.getFreqMatrix(), fpes1.getLabels().toArray(new String[0]));
		double t8 = System.currentTimeMillis();
		FreqEventStructure fes2 = new FreqEventStructure(fpes2.getFreqMatrix(), fpes2.getLabels().toArray(new String[0]));
		double t9 = System.currentTimeMillis();
		
		System.out.println("Frequency Event Structures created");
		
		ComparatorFreq diff = new ComparatorFreq(fes1, fes2);
		//diff.setZeroThreshold(0.05); //combined values should be at least 5%
		diff.setDiffThreshold(0.2);  //0.1 means that there should be at least 10% differences 
									   //in the reported frequencies, in order to be counted as a difference
		diff.combineValues();
		
		System.out.println(diff.getDifferences(true)); 
		double t10 = System.currentTimeMillis();
		
		System.out.println("PES1 time: " + (t3 - t1));
		System.out.println("PES2 time: " + (t4 - t3 + t2 - t1));
		System.out.println("CF verb time: " + (t7 - t6));
		System.out.println("FPES1 time: " + (t6 - t5 + t9 - t8));
		System.out.println("FPES2 time: " + (t5 - t4 + t8 - t7));
		System.out.println("Freq verb time: " + (t10 - t9));
		
		//System.out.println(fes1.toString());
		//System.out.println(fes2.toString());
	}

	public PORuns getRuns(String model) throws Exception {
		XLog log = XLogReader.openLog(String.format(fileName_trace, model));		
		AlphaRelations alphaRelations = new AlphaRelations(log);
		
		File target = new File("target");
		if (!target.exists())
			target.mkdirs();
		
//	    long time = System.nanoTime();
		PORuns runs = new PORuns();

		for (XTrace trace: log) {
			PORun porun = 
					new PORun(alphaRelations, trace);
//					new AbstractingShortLoopsPORun(alphaRelations, trace);
			runs.add(porun);
		}
		
		IOUtils.toFile("target/" + model + "_prefix.dot", runs.toDot());
		runs.mergePrefix();
		IOUtils.toFile("target/" + model + "_merged.dot", runs.toDot());
		
		return runs;
	}
	
	public void printFESToFile(String filename, FreqEventStructure fes) {
		try {
			File newTextFile = new File(filename);
            FileWriter fileWriter = new FileWriter(newTextFile);
            fileWriter.write(fes.toString());
            fileWriter.close();
		}
		catch(Exception e) {
			System.out.println(e.getStackTrace());
		}
	}
}
