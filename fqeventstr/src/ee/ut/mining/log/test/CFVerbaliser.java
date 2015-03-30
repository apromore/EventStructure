package ee.ut.mining.log.test;

import java.util.HashSet;

import ee.ut.core.comparison.ComparatorExecutionGraph;
import ee.ut.core.comparison.differences.Differences;
import ee.ut.core.comparison.verbalizer.VerbalizerAESPES;
import ee.ut.core.eventstr.PES.PrimeEventStructure; 

public class CFVerbaliser {
	
	public static String getCFdifferences(ee.ut.eventstr.OldPrimeEventStructure<Integer> temppes1, ee.ut.eventstr.OldPrimeEventStructure<Integer> temppes2) {
		PrimeEventStructure<String> pes1 = PESconverter.convertBetweenPES(temppes1);
		PrimeEventStructure<String> pes2 = PESconverter.convertBetweenPES(temppes2);

		ComparatorExecutionGraph<String> diff = new ComparatorExecutionGraph<String>(pes1, pes2, new HashSet<String>(pes2.getLabels()), new VerbalizerAESPES<String>());
		
		/* Compare the models using the foldedAES.
		 * The comparator extracts the last event structure computed. */
		Differences differences = diff.getDifferences();
		
		return differences.toString();
	}
}
