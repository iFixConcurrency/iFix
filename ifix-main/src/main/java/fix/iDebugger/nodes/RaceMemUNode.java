package fix.iDebugger.nodes;

import edu.tamu.aser.tide.nodes.MemNode;

import java.util.*;

/**
 * @author ann
 */
public class RaceMemUNode {
	@Override
	public int hashCode() {
		return Objects.hash(raceMap, allUsedLocks, suggestionLock, memSigs, allNodes, sig);
	}

	public static Map<String, RaceMemUNode> sigToNode = new HashMap<>();
	public static Map<String, RaceMemUNode> sigToNode2 = new HashMap<>();
	
	private Map<String, HashSet<String>> raceMap;
	
	/**
	 * record all locks and number of times this share variable used
	 */
	private Map<RaceLockUNode, Integer> allUsedLocks;
	
	private RaceLockUNode suggestionLock;
	
	// prevent same var from same line, (read & write)
	private HashSet<String> memSigs;
	
	private Set<MemNode> allNodes;
	
	private String sig;
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
//		builder.append("RaceMemUNode : sig = " + sig + " with " + allNodes.size() + " nodes : ");
		for(MemNode memNode : allNodes){
//			builder.append("[" + memNode.getPrefix() + " in " + memNode.getFile().getName() + " " + memNode.getLine() + "]");
		}
		return builder.toString();
	}
	
	private RaceMemUNode(){
		this.allNodes = new HashSet<>();
		this.memSigs = new HashSet<>();
		this.allUsedLocks = new HashMap<>();
		this.raceMap = new HashMap<>();
	}

	public static RaceMemUNode getNode(String sig){
		if(sigToNode.containsKey(sig)){
			return sigToNode.get(sig);
		}
		RaceMemUNode node = new RaceMemUNode();
		node.sig = sig;
		sigToNode.put(sig, node);
		return node;
	}
	
	public static RaceMemUNode getNode(MemNode memNode){
		if(sigToNode.containsKey(memNode.getObjSig().toString())){
			return sigToNode.get(memNode.getObjSig().toString());
		}
		RaceMemUNode node = new RaceMemUNode();
		node.sig = memNode.getObjSig().toString();
		sigToNode.put(node.sig, node);
		return node;
	}
	
	public static void clear(){
		sigToNode.clear();
		sigToNode2.clear();
	}
	
	public static RaceMemUNode getNode2(MemNode memNode){
		if(sigToNode2.containsKey(memNode.getObjSig().toString())){
			return sigToNode2.get(memNode.getObjSig().toString());
		}
		RaceMemUNode node = new RaceMemUNode();
		node.sig = memNode.getObjSig().toString();
		sigToNode2.put(node.sig, node);
		return node;
	}
	
	public static boolean hasNode(MemNode memNode){
		return sigToNode.containsKey(memNode.getObjSig().toString()) && sigToNode.get(memNode.getObjSig().toString()).allNodes.contains(memNode);
	}
	
	public static void optimize(){
		HashSet<String> lineSet = new HashSet<>();
		HashSet<String> removedItem = new HashSet<>();
		for(String string : sigToNode.keySet()){
			RaceMemUNode node = sigToNode.get(string);
			for(Iterator<MemNode> it = node.allNodes.iterator(); it.hasNext(); ){
				MemNode memNode = it.next();
				if(lineSet.contains(memNode.getSig())){
					it.remove();
				}
				else{
					lineSet.add(memNode.getSig());
				}
			}
			if(node.allNodes.isEmpty()){
				removedItem.add(string);
			}
		}
		for(String s : removedItem){
			sigToNode.remove(s);
		}
		removedItem.clear();
		for(String string : sigToNode2.keySet()){
			RaceMemUNode node = sigToNode2.get(string);
			for(Iterator<MemNode> it = node.allNodes.iterator(); it.hasNext(); ){
				MemNode memNode = it.next();
				if(lineSet.contains(memNode.getSig())){
					it.remove();
				}
				else{
					lineSet.add(memNode.getSig());
				}
			}
			if(node.allNodes.isEmpty()){
				removedItem.add(string);
			}
		}
		for(String s : removedItem){
			sigToNode2.remove(s);
		}
		for(String string : sigToNode2.keySet()){
			if(sigToNode.containsKey(string)){
				sigToNode.get(string).getAllNodes().addAll(sigToNode2.get(string).getAllNodes());
			}
			else{
				sigToNode.put(string, sigToNode2.get(string));
			}
		}
		sigToNode2.clear();
	}
	
	public String getRaceMsg(MemNode memNode){
		String s = memNode.getPrefix() + memNode.getLine();
		if(this.raceMap.containsKey(s)){
			return s + " , " + raceMap.get(s);
		}
		else{
			return "race message not found";
		}
	}
	
	public void setRace(MemNode node1, MemNode node2){
		String s1 = node1.getPrefix() + node1.getLine();
		String s2 = node2.getPrefix() + node2.getLine();
		if(this.raceMap.containsKey(s1)){
			this.raceMap.get(s1).add(s2);
		}
		else{
			HashSet<String> set = new HashSet<>();
			set.add(s2);
			this.raceMap.put(s1, set);
		}
		
		if(this.raceMap.containsKey(s2)){
			this.raceMap.get(s2).add(s1);
		}
		else{
			HashSet<String> set = new HashSet<>();
			set.add(s1);
			this.raceMap.put(s2, set);
		}
	}
	
	public void addLock(RaceLockUNode lock){
		if(allUsedLocks.containsKey(lock)){
			Integer count = allUsedLocks.get(lock);
			count ++;
			allUsedLocks.put(lock, count);
		}
		else{
			allUsedLocks.put(lock, 1);
		}
	}
	
//	public static RepairNode getRepairNode(MemNode node){
//		RaceMemUNode memUNode = getNode(node);
//		RaceLockUNode lockUNode = lock
//	}
	
	public void addUsedLock(RaceLockUNode lock){
		if(! allUsedLocks.containsKey(lock)){
			allUsedLocks.put(lock, 0);
		}
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 == null){
			return false;
		}
		if(arg0 instanceof RaceMemUNode){
			RaceMemUNode node = (RaceMemUNode) arg0;
			return this.sig.equals(node.sig);
		}
		return false;
	}
	
	public void addMemNode(MemNode node){
		if(! this.memSigs.contains(node.getSig())){
			this.allNodes.add(node);
		}
	}
	
	
	
	public RaceLockUNode getSuggestionLock() {
		return suggestionLock;
	}

	public void setSuggestionLock(RaceLockUNode suggestionLock) {
		this.suggestionLock = suggestionLock;
	}

	public Map<RaceLockUNode, Integer> getAllUsedLocks() {
		return allUsedLocks;
	}

	public void setAllUsedLocks(Map<RaceLockUNode, Integer> allUsedLocks) {
		this.allUsedLocks = allUsedLocks;
	}

	public Set<MemNode> getAllNodes() {
		return allNodes;
	}

	public void setAllNodes(Set<MemNode> allNodes) {
		this.allNodes = allNodes;
	}

	public String getSig() {
		return sig;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}

	public Map<String, HashSet<String>> getRaceMap() {
		return raceMap;
	}

	public void setRaceMap(Map<String, HashSet<String>> raceMap) {
		this.raceMap = raceMap;
	}
	
	
	
}
