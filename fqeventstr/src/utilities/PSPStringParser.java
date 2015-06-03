package utilities;

import ee.ut.eventstr.comparison.PartialSynchronizedProduct;

public class PSPStringParser {
	
	private PartialSynchronizedProduct<Integer> psp;
	
	public PSPStringParser(PartialSynchronizedProduct<Integer> psp) {
		this.psp = psp;
	}
	
	public String getHides() {
		String pspdot = psp.toDot();
		String hides = "";
		
		int pos = pspdot.indexOf("->");
		int posend;
		String statement;
		Boolean curhide = false;
		
		while (pos > 0) {
			pos = pspdot.indexOf("[", pos) + 8;
			posend = pspdot.indexOf("]", pos) - 1;
			
			statement = pspdot.substring(pos, posend);
			
			if (!statement.startsWith("match")) {
				if (!curhide) hides += "\r\n"; 
				hides += statement + "\r\n";
				curhide = true;
			}
			else {
				curhide = false;
			}
			
			pos = pspdot.indexOf("->", pos + 1);
		}
		
		return hides.substring(1); //remove first carriage return
	}
	
}
