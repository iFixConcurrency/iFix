package fix.iDebugger.nodes;

import java.util.HashSet;

public class RaceConstraintRepository {
	private HashSet<RaceLockUNode> lockList;
	private HashSet<RaceMemUNode> variableList;
	
	public RaceConstraintRepository(){
		this.lockList = new HashSet<>();
		this.variableList = new HashSet<>();
	}
	
	public RaceConstraintRepository(HashSet<RaceLockUNode> lockList, HashSet<RaceMemUNode> variableList) {
		super();
		this.lockList = lockList;
		this.variableList = variableList;
	}
	
	public void addLock(RaceLockUNode lock){
		this.lockList.add(lock);
	}
	
	public void addVariable(RaceMemUNode variable){
		this.variableList.add(variable);
	}

	public HashSet<RaceLockUNode> getLockList() {
		return lockList;
	}

	public void setLockList(HashSet<RaceLockUNode> lockList) {
		this.lockList = lockList;
	}

	public HashSet<RaceMemUNode> getVariableList() {
		return variableList;
	}

	public void setVariableList(HashSet<RaceMemUNode> variableList) {
		this.variableList = variableList;
	}
	
	public void clear(){
		this.variableList.clear();
		this.lockList.clear();
	}
	
}
