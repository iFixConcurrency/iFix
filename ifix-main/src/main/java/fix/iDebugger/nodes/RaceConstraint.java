package fix.iDebugger.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class RaceConstraint {
	private List<RaceConstraintNode> list;

	public static Map<RaceLockUNode, HashSet<RaceConstraintNode>> lockToConstraint = new HashMap<>();
	
	public RaceConstraint(){
		this.list = new ArrayList<>();
	}
	
	public static void clear(){
		lockToConstraint.clear();
	}
	
	public RaceConstraint(List<RaceConstraintNode> list) {
		super();
		this.list = list;
	}
	
	public void initConstraints(RaceConstraintRepository depository){
		for(RaceMemUNode node : depository.getVariableList()){
			for(RaceLockUNode lock : depository.getLockList()){
				addItemA(node, lock, 0);
			}
		}
	}
	
	private RaceConstraintNode makeNode(Object var1, Object var2, RaceConstraintType type, int score) {
		RaceConstraintNode constraintNode = new RaceConstraintNode();
		constraintNode.setVar(var1, var2, type, score);
		return constraintNode;
	}
	
	public RaceConstraintNode getNode(RaceMemUNode node, RaceLockUNode lock) {
		for(int i = 0; i < list.size(); i ++){
			if(list.get(i).getType() == RaceConstraintType.A){
				RaceConstraintNode tNode = list.get(i);
				if(node.equals(((RaceMemUNode) tNode.getVar1())) && lock.equals((RaceLockUNode) tNode.getVar2())){
					return tNode;
				}
			}
		}
		return null;
	}
	
	// only used for initiate
	public void addItemA(Object node, Object lock, int score){
		RaceConstraintNode constraintNode = makeNode(node, lock, RaceConstraintType.A, score);
		list.add(constraintNode);
		
		if(lockToConstraint.containsKey(lock)){
			lockToConstraint.get(lock).add(constraintNode);
		}
		else{
			HashSet<RaceConstraintNode> tSet = new HashSet<>();
			tSet.add(constraintNode);
			lockToConstraint.put((RaceLockUNode)lock, tSet);
		}
	}
	
	public void addItemB(Object node1, Object node2, int score){
		RaceConstraintNode constraintNode = makeNode(node1, node2, RaceConstraintType.B, score);
		list.add(constraintNode);
	}

	public List<RaceConstraintNode> getList() {
		return list;
	}

	public void setList(List<RaceConstraintNode> list) {
		this.list = list;
	}

	
	
}
