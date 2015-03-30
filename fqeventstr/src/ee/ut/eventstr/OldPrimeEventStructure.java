package ee.ut.eventstr;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jbpt.graph.DirectedGraph;
import org.jbpt.hypergraph.abs.Vertex;

import ee.ut.eventstr.util.PES2Net;

public class OldPrimeEventStructure<T> {
	protected BehaviorRelation[][] matrix;
	protected boolean[][] dcausality;
	protected List<List<Integer>> postset;
	protected List<List<Integer>> preset;
	protected Map<T, Integer> map;
	protected List<T> ridx;
	protected Map<Integer, Map<Integer, Set<Integer>>> preconflictEvents;

	protected List<String> labels;
	private String modelName;

	public OldPrimeEventStructure(BehaviorRelation[][] matrix, boolean[][] dcausality,
			Map<T, Integer> map, List<T> ridx, List<String> labels, String modelName) {
		this.matrix = matrix;
		this.map = map;
		this.labels = labels;
		this.ridx = ridx;
		this.modelName = modelName;
		this.dcausality = dcausality;
		
		initDCausalityLists();
	}
	
	private void initDCausalityLists() {
		postset	= new ArrayList<>(matrix.length);
		preset = new ArrayList<>(matrix.length);

		for (int i = 0; i < matrix.length; i++) {
			postset.add(new ArrayList<Integer>());
			preset.add(new ArrayList<Integer>());
		}

		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix.length; j++)
				if (dcausality[i][j]) {
					postset.get(i).add(j);
					preset.get(j).add(i);
				}
	}

	public List<T> getReverseIndex() { return ridx; }
	
	public String getModelName() {
		return modelName;
	}

	public void printBRelMatrix(PrintStream out) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				out.printf("%s ", getCharacter(matrix[i][j]));
			}
			out.println();
		}
	}

	private String getCharacter(BehaviorRelation behaviorRelation) {
		switch (behaviorRelation) {
		case CAUSALITY:
			return "<";
		case INV_CAUSALITY:
			return ".";
		case CONFLICT:
			return "#";
		case CONCURRENCY:
			return "|";
		case ASYM_CONFLICT:
			return "/";
		case INV_ASYM_CONFLICT:
			return ".";
		}
		return null;
	}

	public Set<Integer> getIndexes(String... strings) {
		Set<String> strs = new HashSet<String>();
		Set<Integer> indexes = new HashSet<Integer>();

		for (String s : strings)
			strs.add(s);

		int i = 0;
		for (String l : labels) {
			if (strs.contains(l))
				indexes.add(i);
			i++;
		}

		return indexes;
	}

	public List<String> getLabels() {
		return labels;
	}

	public String getLabel(T event) {
		return labels.get(map.get(event));
	}

	public T getEvent(Integer i) {
		return ridx.get(i);
	}
	
	public BehaviorRelation getERelation(T t, T t2) {
		return matrix[map.get(t)][map.get(t2)];
	}

	public Integer getIndex(T t) {
		return map.get(t);
	}

	public BehaviorRelation getIRelation(Integer i, Integer j) {
		return matrix[i][j];
	}

//	public void toLatex(PrintStream out) {
//		out.print("\\documentclass{article}\n");
//		out.print("\\usepackage{tikz}\n");
//		out.print("\\usepackage[paperheight=15in,paperwidth=8.5in]{geometry}\n");
//		out.print("\\usetikzlibrary{arrows,shapes}\n");
//		out.print("\\usepackage{dot2texi}\n");
//		out.print("\\begin{document}\n");
//
//		out.print("\\begin{tikzpicture}[>=stealth',scale=0.4]\n");
//		out.print("\\tikzstyle{causality} = [draw, thick]\n");
//		out.print("\\begin{dot2tex}[dot,tikz,codeonly,styleonly,options=-s]\n");
//
//		out.print("digraph G {\n");
//
//		for (int i = 0; i < ridx.size(); i++)
//			out.printf("\tn%d [label=\"%s(%d)\"];\n", i, labels.get(i), i);
//
//		out.print("\tedge [style=\"causality\"];\n");
//
//		for (int i = 0; i < ridx.size(); i++)
//			for (int j = 0; j < ridx.size(); j++) {
//				if (dcausality[i][j])
//					out.printf("\tn%d -> n%d;\n", i, j);
//			}
//
//		out.print("}\n");
//		out.print("\\end{dot2tex}\n");
//
//		for (Set<Integer> cluster : PES2Net.getConflictClusters(this))
//			for (Integer src : cluster)
//				for (Integer tgt : cluster)
//					if (src < tgt)
//						out.printf(
//								"\\draw[red,thick]  [bend right] (n%d) to [bend left] (n%d);\n",
//								src, tgt);
//
//		out.print("\\end{tikzpicture}\n");
//		out.print("\\end{document}\n");
//	}

	public List<Integer> getDirectPredecessors(Integer target) {
		return preset.get(target);
	}

	public List<Integer> getDirectSuccessors(Integer source) {
		return postset.get(source);
	}

	public Set<Integer> getSinkNodes() {
		Set<Integer> result = new HashSet<Integer>();
		for (int i = 0; i < postset.size(); i++)
			if (postset.get(i).size() == 0)
				result.add(i);
		return result;
	}

	public Set<Integer> getSourceNodes() {
		Set<Integer> result = new HashSet<Integer>();
		for (int i = 0; i < preset.size(); i++)
			if (preset.get(i).size() == 0)
				result.add(i);
		return result;
	}

	public Set<Integer> conflictSet(Integer pivot) {
		Set<Integer> result = new HashSet<Integer>();
		for (int i = 0; i < matrix.length; i++)
			if (matrix[pivot][i] == BehaviorRelation.CONFLICT)
				result.add(i);
		return result;
	}
	
	public Set<Integer> concurrentSet(Integer pivot) {
		Set<Integer> result = new HashSet<Integer>();
		for (int i = 0; i < matrix.length; i++)
			if ( i != pivot && matrix[pivot][i] == BehaviorRelation.CONCURRENCY)
				result.add(i);
		return result;
	}

	private DirectedGraph subgraph(Integer sink) {
		Queue<Integer> open = new LinkedList<>();
		open.add(sink);
		Set<Integer> visited = new HashSet<Integer>();
		while (!open.isEmpty()) {
			Integer curr = open.poll();
			visited.add(curr);
			for (Integer pred : getDirectPredecessors(curr))
				if (!open.contains(pred) && !visited.contains(pred))
					open.add(pred);
		}

		DirectedGraph sub = new DirectedGraph();
		Map<Integer, Vertex> map = new HashMap<>();

		for (Integer e : visited) {
			Vertex v = new Vertex(labels.get(e));
			map.put(e, v);
//			rmap.put(v, e);
		}

		for (Integer src : visited)
			for (Integer tgt : visited)
				// if (dcausality[src][tgt])
				if (matrix[src][tgt] == BehaviorRelation.CAUSALITY)
					sub.addEdge(map.get(src), map.get(tgt));

		return sub;
	}

	public boolean directSuccessor(Integer src, Integer tgt) {
		return dcausality[src][tgt];
	}

	public BehaviorRelation[][] getBRelMatrix() {
		return this.matrix;
	}

	public Map<T, Integer> getEvent2IndexMap() {
		return this.map;
	}

	public void setRelation(Integer j, Integer k, BehaviorRelation relation) {
		matrix[j][k] = relation;
	}

	public void updateRelation(Integer j, Integer k, BehaviorRelation relation) {
		if (relation == BehaviorRelation.CAUSALITY) {
			matrix[j][k] = BehaviorRelation.CAUSALITY;
			matrix[k][j] = BehaviorRelation.INV_CAUSALITY;
			dcausality[j][k] = true;
		} else {
			matrix[j][k] = relation;
			matrix[k][j] = relation;
			dcausality[j][k] = false;
			dcausality[k][j] = false;
		}
	}
	
	public void printConfiguration(Set<Integer> configuration) {
		boolean firsttime = true;
		for (Integer node : configuration) {
			if (firsttime)
				firsttime = false;
			else
				System.out.print(", ");
			System.out.printf("%s(%d)", labels.get(node), node);
		}
		System.out.println();
	}

//	public boolean areCausalyRelated(Integer e1, Integer e2) {
//		return matrix[e1][e2] == BehaviorRelation.CAUSALITY || matrix[e2][e1] == BehaviorRelation.CAUSALITY;
//	}
//	
//	public boolean firstIsCausalPredecessorOfSecond(Integer pred, Integer succ) {
//		return matrix[pred][succ] == BehaviorRelation.CAUSALITY;
//	}
//
//	public Set<Integer> getSuccessorsOf(Integer event) {
//		Set<Integer> successors = new HashSet<Integer>();
//		for (int i = 0; i < matrix.length; i++)
//			if (matrix[event][i] == BehaviorRelation.CAUSALITY)
//				successors.add(i);
//		return successors;
//	}
//
//	public Set<Integer> getPredecessorsOf(Integer event) {
//		Set<Integer> predecessors = new HashSet<Integer>();
//		for (int i = 0; i < matrix.length; i++)
//			if (matrix[i][event] == BehaviorRelation.CAUSALITY)
//				predecessors.add(i);
//		return predecessors;
//	}
	public String toDot(BitSet conf, BitSet hidings) {
		StringWriter str = new StringWriter();
		PrintWriter out = new PrintWriter(str);
		
		out.println("digraph G {");
		
		out.println("\tnode[shape=box];");
		for (int e = conf.nextSetBit(0); e >= 0; e = conf.nextSetBit(e+1)) {
			if (!hidings.get(e))
				out.printf("\tn%d [label=\"%s\"];\n", e, labels.get(e));
		}

		out.println("\tnode[shape=box,style=filled,color=red];");
		for (int e = hidings.nextSetBit(0); e >= 0; e = hidings.nextSetBit(e+1)) {
				out.printf("\tn%d [label=\"%s\"];\n", e, labels.get(e));			
		}
		
		for (int src = conf.nextSetBit(0); src >= 0; src = conf.nextSetBit(src+1))
			for (int tgt = conf.nextSetBit(0); tgt >= 0; tgt = conf.nextSetBit(tgt+1))
				if (dcausality[src][tgt])
					out.printf("\tn%d -> n%d;\n", src, tgt);
		
		out.println("}");
		
		return str.toString();
	}

}