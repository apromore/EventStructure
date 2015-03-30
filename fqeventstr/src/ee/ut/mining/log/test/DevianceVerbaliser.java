package ee.ut.mining.log.test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.utils.IOUtils;

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

public class DevianceVerbaliser {
	
	ConcurrentHashMap<Integer, PrimeEventStructure<Integer>> pesMap = new ConcurrentHashMap<Integer, PrimeEventStructure<Integer>>();
	ConcurrentHashMap<Integer, FreqEventStructure> fesMap = new ConcurrentHashMap<Integer, FreqEventStructure>();
	ConcurrentHashMap<Integer, Boolean> outputMap = new ConcurrentHashMap<Integer, Boolean>();
	
	Lock lock = new ReentrantLock();
	Condition condition = lock.newCondition();
	
	public static final String version = "0.1";
	
	// static void main for eclipse testing
	public static void main(String[] args) throws Exception {
		DevianceVerbaliser flt = new DevianceVerbaliser();
		
		flt.getAllDifferences(args);
	}
		
	public void getAllDifferences(String[] args) throws Exception {
		double t = System.currentTimeMillis();
		
		String model1, model2, inputfolder, outputfolder, fileNameTrace1, fileNameTrace2;
		String primeOutput = "";
		String freqOutput = "";
		double diffTH = 0;
		double zeroTH = 0;
		
		if ((args.length == 6) || (args.length == 8)) {
			inputfolder = args[0];
			inputfolder = inputfolder.replace("\\", "/");
			if (!inputfolder.endsWith("/")) inputfolder += "/";
			
			model1 = args[1];
			model2 = args[2];
			
			outputfolder = args[3];
			outputfolder = outputfolder.replace("\\", "/");
			if (!outputfolder.endsWith("/")) outputfolder += "/";
					
			fileNameTrace1 = inputfolder + "%" + "s" + model1.substring(model1.lastIndexOf("."));
			fileNameTrace2 = inputfolder + "%" + "s" + model2.substring(model2.lastIndexOf("."));
			
			primeOutput = outputfolder + args[4];
			freqOutput = outputfolder + args[5];
			
			if (args.length == 8) {
				diffTH = Double.parseDouble(args[6]);
				zeroTH = Double.parseDouble(args[7]);
			}
		}
		else {
			if (args.length == 1) { 
				switch (args[0]) {
				case "--version": 
					System.out.println("Version: " + version);
					break;
				case "--about":
					System.out.println("Deviance Verbaliser");
					System.out.println();
					System.out.println("Version: " + version);
					System.out.println("Build date: " + "22-03-2015");
					System.out.println();

					System.out.println("Developed by:");
					System.out.println("Dr. N.R.T.P. van Beest");
					System.out.println("Dr. L. Garcia-Bañuelos");
					break;
				}
				return;
			}
			
			System.out.println("Wrong number of parameters.");
			System.out.println("Check readme.txt for an overview.");
			return;
		}		
				
		new Thread(new Executor(1, model1, fileNameTrace1)).start();
	    new Thread(new Executor(2, model2, fileNameTrace2)).start();
		
		// wait for the threads to finish
	    lock.lock();
	    while ((pesMap.size() < 2) || (fesMap.size() < 2)) {
	    	condition.await();
	    }
	    lock.unlock();
	    
		System.out.println("Differences created, verbalising differences...");
		
		new Thread(new PrimeVerbaliser(primeOutput)).start();
		
		new Thread(new FreqVerbaliser(freqOutput, diffTH, zeroTH)).start();
		
		// wait for the threads to finish
		lock.lock();
		while (outputMap.size() < 2) {
			condition.await();
		}
		lock.unlock();
		
		// print total execution time
		System.out.println(System.currentTimeMillis() - t + " ms");
	}
	
	class Executor implements Runnable {
		private int index;
		private String model;
		private String fileNameTrace;
		
		public Executor(int index, String model, String fileNameTrace) {
			this.index = index;
			this.model = model;
			this.fileNameTrace = fileNameTrace;
		}

		public PORuns getRuns() throws Exception {
			XLog log = XLogReader.openLog(String.format(fileNameTrace, model));		
			AlphaRelations alphaRelations = new AlphaRelations(log);
			
			PORuns runs = new PORuns();

			for (XTrace trace: log) {
				PORun porun = new PORun(alphaRelations, trace);
				runs.add(porun);
			}
			
			runs.mergePrefix();
			return runs;
		}
		
		@Override
		public void run() {
			try {
				PORuns runs = getRuns();
				
				PrimeEventStructure<Integer> pes = PORuns2PES.getPrimeEventStructure(runs, model);
				FrequencyAwarePrimeEventStructure<Integer> fpes = PORuns2PES.getFrequencyAwarePrimeEventStructure(runs, model);

				System.out.println("PES" + index + " created");
			
				pesMap.put(index, pes);

				String freqLabels[] = pes.getLabels().toArray(new String[0]);
				
				FreqEventStructure fes = new FreqEventStructure(fpes.getFreqMatrix(), fpes.getLabels().toArray(new String[0]));
				System.out.println("FPES" + index + " created: " + freqLabels.length + " labels");
				fesMap.put(index, fes);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			lock.lock();
			condition.signalAll();
			lock.unlock();
		}
	}
	
	class PrimeVerbaliser implements Runnable {
		private String outputfile;
		
		public PrimeVerbaliser(String outputfile) {
			this.outputfile = outputfile;
		}
		
		@Override
		public void run() {
			String totaldiff = "";
			// obtain prime event structures and verbalise		
			PartialSynchronizedProduct<Integer> psp = 
					new PartialSynchronizedProduct<>(new PESSemantics<>(pesMap.get(1)), new PESSemantics<>(pesMap.get(2)));
			
			//remove duplicates
			Set<String> diffset = new HashSet<String>(psp.perform().prune().getDiff());
			
			psp.shortestPathDijkstra();
			
			for (String diff: diffset) {
				totaldiff += "DIFF: " + diff + "\n";
			}
			
			
			if (outputfile.equals("")) {
				System.out.println(totaldiff);
			}
			else {
				IOUtils.toFile(outputfile, totaldiff);
			}
			
			outputMap.put(1, true);
			
			lock.lock();
			condition.signalAll();
			lock.unlock();
			
			System.out.println("Control-flow differences done \n");
		}
	}
	
	class FreqVerbaliser implements Runnable {
		private String outputfile;
		private double diffTH;
		private double zeroTH;
		
		public FreqVerbaliser(String outputfile, double diffTH, double zeroTH) {
			this.outputfile = outputfile;
			this.diffTH = diffTH;
			this.zeroTH = zeroTH;
		}
		
		@Override
		public void run() {
			//obtain frequency event structures and verbalise
			ComparatorFreq diff = new ComparatorFreq(fesMap.get(1), fesMap.get(2));
			diff.setZeroThreshold(zeroTH);
			diff.setDiffThreshold(diffTH);
			diff.combineValues();
			
			if (outputfile.equals("")) {
				System.out.println(diff.getDifferences(true));
			}
			else {
				IOUtils.toFile(outputfile, diff.getDifferences(true));
			}
			
			outputMap.put(2, true);
			
			lock.lock();
			condition.signalAll();
			lock.unlock();
			
			System.out.println("Frequency differences done \n");
		}
	}
}
