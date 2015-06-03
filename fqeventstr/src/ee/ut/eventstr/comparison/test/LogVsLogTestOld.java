package ee.ut.eventstr.comparison.test;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.utils.IOUtils;
import org.junit.Test;

import utilities.PSPStringParser;
import utilities.FES.FreqEventStructure;
import ee.ut.eventstr.PESSemantics;
import ee.ut.eventstr.PrimeEventStructure;
import ee.ut.eventstr.comparison.PartialSynchronizedProduct;
import ee.ut.eventstr.util.PORuns2PES;
import ee.ut.mining.log.AlphaRelations;
import ee.ut.mining.log.PORun;
import ee.ut.mining.log.PORuns;
import ee.ut.mining.log.XLogReader;

public class LogVsLogTestOld {
	String prefix = 
//			"cf_"
//			"cp_"
//			"pl_"
			"re_"
			;
	
	String logfile1 = 
//			"sublog_1050_1200"
			"sublog_1000_1999"
			;
	
	String logfile2 = 
//			"sublog_800_950"
			"sublog_0_999"
			;
	
	String logfiletemplate  = 
			"E:/documents/nicta/testlogs/%s.mxml"
			;
	
	@Test
	public void testCode() throws Exception {
		logfile1 = prefix + logfile1;
		logfile2 = prefix + logfile2;
		
		System.out.println(String.format("E:/documents/nicta/testlogs/target/%s_X_%s_psp.dot", logfile1, logfile2));
		
		PORuns runs1 = getRuns(logfile1);
		PORuns runs2 = getRuns(logfile2);
		
		PrimeEventStructure<Integer> pes1 = PORuns2PES.getPrimeEventStructure(runs1, logfile1);
		PrimeEventStructure<Integer> pes2 = PORuns2PES.getPrimeEventStructure(runs2, logfile2);
		
		PartialSynchronizedProduct<Integer> psp = 
				new PartialSynchronizedProduct<>(new PESSemantics<>(pes1), new PESSemantics<>(pes2));
		
		//remove duplicates
		Set<String> diffset = new HashSet<String>(psp.perform()
				.prune()
				.getDiff());
		
		psp.shortestPathDijkstra();
		
		PSPStringParser parser = new PSPStringParser(psp);
		IOUtils.toFile(String.format("E:/documents/nicta/testlogs/target/%s_X_%s_psp.txt", logfile1, logfile2), parser.getHides());		
		
		IOUtils.toFile(String.format("E:/documents/nicta/testlogs/target/%s_X_%s_psp.dot", logfile1, logfile2), psp.toDot());		

		for (String diff: diffset) {
			System.out.println("DIFF: " + diff);
		}
		
	}

	public PORuns getRuns(String model) throws Exception {
		XLog log = XLogReader.openLog(String.format(logfiletemplate, model));		
		AlphaRelations alphaRelations = new AlphaRelations(log);
		
		PORuns runs = new PORuns();

		for (XTrace trace: log) {
			PORun porun = 
					new PORun(alphaRelations, trace);
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
