//package edu.tamu.aser.tide.views.fix;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.Set;
//
//import org.eclipse.jface.resource.ImageDescriptor;
//
//import edu.tamu.aser.tide.engine.TIDERace;
//import edu.tamu.aser.tide.nodes.MemNode;
//import ann.iDebugger.nodes.RaceMemUNode;
//import ann.iDebugger.nodes.RepairNode;
//import ann.iDebugger.nodes.RepairPolicy;
//
//public class PFixSubNode extends PFixTreeNode {
//	protected String name;
//
//	private int score;
//	private RaceMemUNode memUNode;
//	private RepairNode repairNode;
//
//	public PFixSubNode(PFixTreeNode parent, RaceMemUNode memUNode, RepairNode repairNode, int score) {
//		super(parent);
//		this.memUNode = memUNode;
//		this.repairNode = repairNode;
//		String variable = memUNode.getSig();
//		for(MemNode memNode : memUNode.getAllNodes()){
//			variable = memNode.getPrefix();
//			break;
//		}
//
//		Map<String, Set<Integer>> map = new HashMap<>();
//		for(MemNode memNode : memUNode.getAllNodes()){
//			String file = memNode.getFile().toString();
//			file = file.substring(file.lastIndexOf("/") + 1);
//			if(map.containsKey(file)){
//				map.get(file).add(memNode.getLine());
//			}
//			else{
//				Set<Integer> set2 = new HashSet<>();
//				set2.add(memNode.getLine());
//				map.put(file, set2);
//			}
//		}
//		this.name = "Shared variable " + variable;
//		for(String string : map.keySet()){
//			this.name += " in file " + string + "[ ";
//			for(Integer integer : map.get(string)){
//				this.name += integer + " ";
//			}
//			this.name += "] ";
//		}
//
//		if(repairNode.isNew()){
//			this.name += " use new global lock";
//		}
//		else{
//			this.name += " use lock " + repairNode.getClassName() + "." + repairNode.getLockName() + " : " + repairNode.getLine();
//		}
//
//
//	}
//
//	@Override
//	public String toString() {
//		// TODO Auto-generated method stub
//		return this.name;
//	}
//
//	@Override
//	public String getName() {
//		return name;
//	}
//
//	@Override
//	public ImageDescriptor getImage() {
//		return null;
//	}
//
//	@Override
//	protected void createChildren(ArrayList<LinkedList<String>> events, String fix) {
//		// TODO Auto-generated method stub
//	}
//
//	public int getScore() {
//		return score;
//	}
//
//	public void setScore(int score) {
//		this.score = score;
//	}
//
//	public RaceMemUNode getMemUNode() {
//		return memUNode;
//	}
//
//	public void setMemUNode(RaceMemUNode memUNode) {
//		this.memUNode = memUNode;
//	}
//
//	public RepairNode getRepairNode() {
//		return repairNode;
//	}
//
//	public void setRepairNode(RepairNode repairNode) {
//		this.repairNode = repairNode;
//	}
//
//
//}
