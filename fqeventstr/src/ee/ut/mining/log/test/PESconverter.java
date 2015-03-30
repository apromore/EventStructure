package ee.ut.mining.log.test;

import java.util.List;

import ee.ut.core.eventstr.PES.PrimeEventStructure;

public class PESconverter {
	
	private static String[] createEventsFromLabels(String[] labels) {
		String[] ev = new String[labels.length];
		for (int i = 0; i < labels.length; i++) {
			ev[i] = labels[i] + i;
		}
		return ev;
	}
	
	public static ee.ut.core.eventstr.PES.PrimeEventStructure<String> convertBetweenPES(ee.ut.eventstr.OldPrimeEventStructure<Integer> temppes) {
		ee.ut.core.eventstr.PES.PrimeEventStructure<String> pes;
		
		ee.ut.eventstr.BehaviorRelation[][] br = temppes.getBRelMatrix();
		ee.ut.core.eventstr.BehavioralRelation[][] newbr = new ee.ut.core.eventstr.BehavioralRelation[br.length][br.length];
		
		String[] labels = new String[br.length];
		String[] events = new String[br.length];
		
		for (int i = 0; i < br.length; i++) {
			for (int j = 0; j < br.length; j++) {
				newbr[i][j] = ee.ut.core.eventstr.BehavioralRelation.valueOf(br[i][j].toString());
			}
		}
		
		List<String> templabels = temppes.getLabels();
		
		for (int i = 0; i < templabels.size(); i++) {
			labels[i] = templabels.get(i);
		}
		
		//labels = temppes.getFreqLabels();
		events = createEventsFromLabels(labels);
		
		pes = new PrimeEventStructure<String>(newbr, events, labels);
		
		return pes;
	}
}
