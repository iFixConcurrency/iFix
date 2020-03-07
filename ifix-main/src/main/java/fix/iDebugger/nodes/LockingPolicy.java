package fix.iDebugger.nodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.ibm.wala.util.collections.Pair;

public class LockingPolicy implements Comparable<LockingPolicy> {
	
	private int score;
	
	private List<Pair<RaceMemUNode, RaceLockUNode>> repairList;

	public LockingPolicy(){
		this.score = -1;
		this.repairList = new ArrayList<>();
	}
	
	public void addPair(Pair<RaceMemUNode, RaceLockUNode> pair){
		this.repairList.add(pair);
	}
	
	@Override
	public int compareTo(LockingPolicy lockingPolicy){
		return this.score - lockingPolicy.score;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public List<Pair<RaceMemUNode, RaceLockUNode>> getRepairList() {
		return repairList;
	}

	public void setRepairList(List<Pair<RaceMemUNode, RaceLockUNode>> repairList) {
		this.repairList = repairList;
	}
	
	public void printLockInfo(){
		HashSet<RepairNode> lockList = new HashSet<>();
		for(Pair<RaceMemUNode, RaceLockUNode> pair : repairList){
			lockList.add(pair.snd.getRepairNode());
		}
	}

	public String getMessage(){
		StringBuilder builder = new StringBuilder();
		for(Pair<RaceMemUNode, RaceLockUNode> pair : this.getRepairList()){
			builder.append("add lock ");
			if(pair.snd.getRepairNode() != null){
				builder.append(pair.snd.getRepairNode().getLockName());
			}
			builder.append(" to variable " + pair.fst.getSig());
//			builder.append(pair.fst).append(" using lock ").append(pair.snd).append(" | ");
		}
		return builder.toString();
	}
	
	
	
}
