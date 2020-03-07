package fix.iDebugger.nodes;

import java.util.*;

import edu.tamu.aser.tide.nodes.DLockNode;

public class RaceLockUNode {
	@Override
	public int hashCode() {
		return Objects.hash(allNodes, sig, repairNode);
	}

	public static Map<String, RaceLockUNode> sigToNode = new HashMap<>();
	
	public static int lockIndex = 0;
	
	private Set<DLockNode> allNodes;
	
	private String sig;
	
	private RepairNode repairNode;
	
	private RaceLockUNode(){
	}
	
	public static RaceLockUNode getNode(DLockNode lockNode){
		if(sigToNode.containsKey(lockNode.getLockSig().toString())){
			return sigToNode.get(lockNode.getLockSig().toString());
		}
		RaceLockUNode node = new RaceLockUNode();
		node.sig = lockNode.getLockSig().toString();
		node.allNodes = new HashSet<>();
		sigToNode.put(node.sig, node);
		return node;
	}
	
	public static void optimize(){
	    for(String string : sigToNode.keySet()){
	    	sigToNode.get(string).generateRepairNode();
	    }
	}
	
	public static void clear(){
		sigToNode.clear();
		lockIndex = 0;
	}
	
	public void generateRepairNode(){
		if(this.allNodes.size() == 0){
			return;
		}
		
		DLockNode lockNode = null;
		for(DLockNode dLockNode : allNodes){
			lockNode = dLockNode;
			break;
		}
		
		this.repairNode = new RepairNode(lockNode, 1);
	}
	
	public static void generateStaticLock(){
		RaceLockUNode lockUNode = new RaceLockUNode();
		lockUNode.allNodes = new HashSet<>();
		lockUNode.sig = "static lock " + lockIndex ++;
		RepairNode repairNode = RepairNode.getNewStaticLock("", "");
		lockUNode.setRepairNode(repairNode);
		sigToNode.put(lockUNode.sig, lockUNode);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("RaceLockUNode : sig = " + sig + " with " + allNodes.size() + " nodes");
		return builder.toString();
	}
	
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 == null){
			return false;
		}
		if(arg0 instanceof RaceLockUNode){
			RaceLockUNode node = (RaceLockUNode) arg0;
			return this.sig.equals(node.sig);
		}
		return false;
	}
	
	public void addLockNode(DLockNode node){
		this.allNodes.add(node);
	}
	
	public Set<DLockNode> getAllNodes() {
		return allNodes;
	}

	public void setAllNodes(Set<DLockNode> allNodes) {
		this.allNodes = allNodes;
	}

	public String getSig() {
		return sig;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}

	public RepairNode getRepairNode() {
		return repairNode;
	}

	public void setRepairNode(RepairNode repairNode) {
		this.repairNode = repairNode;
	}
	
	
}
