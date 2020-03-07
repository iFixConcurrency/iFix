package fix.iDebugger.nodes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.ibm.wala.util.collections.Pair;

public class RepairFeedBack {

	private Queue<LockingPolicy> queue;
	
	private static RepairFeedBack instance;
	
	private Map<Pair<RaceMemUNode,RaceLockUNode>, Integer> vlMap;
	
	private final static int NUM_OF_QUEUE = 3;
	
	private RepairFeedBack(){
		queue = new LinkedList<>();
		vlMap = new HashMap<>();
	}
	
	public static RepairFeedBack getInstance(){
		if(instance == null){
			instance = new RepairFeedBack();
		}
		return instance;
	}
	
	public void addNewPolicy(LockingPolicy lockingPolicy){
		// remove policy
		if(queue.size() >=  NUM_OF_QUEUE){
			for(Pair<RaceMemUNode, RaceLockUNode> pair : lockingPolicy.getRepairList()){
				if(vlMap.containsKey(pair)){
					Integer tmp = vlMap.get(pair);
					tmp --;
					if(tmp == 0){
						vlMap.remove(pair);
					}
					else{
						vlMap.put(pair, tmp);
					}
				}
			}
			queue.poll();
		}
		
		// add policy
		queue.add(lockingPolicy);
		
		for(Pair<RaceMemUNode, RaceLockUNode> pair : lockingPolicy.getRepairList()){
			if(vlMap.containsKey(pair)){
				Integer tmp = vlMap.get(pair);
				tmp ++;
				vlMap.put(pair, tmp);
			}
			else{
				vlMap.put(pair, 1);
			}
		}
	}
	
	public int getScore(RaceMemUNode node, RaceLockUNode lockUNode){
		Pair<RaceMemUNode, RaceLockUNode> pair = Pair.make(node, lockUNode);
		if(vlMap.containsKey(pair)){
			return vlMap.get(pair);
		}
		else{
			return 0;
		}
	}
}
