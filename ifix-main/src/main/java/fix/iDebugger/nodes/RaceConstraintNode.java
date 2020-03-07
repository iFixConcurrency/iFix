package fix.iDebugger.nodes;

import java.util.Map;

public class RaceConstraintNode {
	// A stands for lock(a) = a
	// B stands for lock(a) = lock(b)
	
	private RaceConstraintType type;
	
	private int score;
	
	private Object var1;
	private Object var2;
	
	public void setVar(Object var1, Object var2, RaceConstraintType type, int score){
		this.var1 = var1;
		this.var2 = var2;
		this.type = type;
		this.score = score;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RaceConstraintNode other = (RaceConstraintNode) obj;
		if (type != other.type)
			return false;
		if (var1 == null) {
			if (other.var1 != null)
				return false;
		} else if (!var1.equals(other.var1))
			return false;
		if (var2 == null) {
			if (other.var2 != null)
				return false;
		} else if (!var2.equals(other.var2))
			return false;
		return true;
	}
	
	public RaceConstraintType getType() {
		return type;
	}

	public void setType(RaceConstraintType type) {
		this.type = type;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public Object getVar1() {
		return var1;
	}

	public void setVar1(Object var1) {
		this.var1 = var1;
	}

	public Object getVar2() {
		return var2;
	}

	public void setVar2(Object var2) {
		this.var2 = var2;
	}
	
	public boolean isSatisfied(Map<RaceMemUNode, RaceLockUNode> map){
		if(this.type == RaceConstraintType.A){
			RaceMemUNode var1 = (RaceMemUNode) this.var1;
			RaceLockUNode var2 = (RaceLockUNode) this.var2;
			if(var1 == null || var2 == null){
				return false;
			}
			if(! map.containsKey(var1)){
				return false;
			}
			return map.get(var1).equals(var2);
		}
		else if(this.type == RaceConstraintType.B){
			RaceMemUNode var1 = (RaceMemUNode) this.var1;
			RaceMemUNode var2 = (RaceMemUNode) this.var2;
			if(var1 == null || var2 == null){
				return false;
			}
			if(! map.containsKey(var1) || ! map.containsKey(var2)){
				return false;
			}
			return map.get(var1).equals(map.get(var2));
		}
		return false;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		if(this.type == RaceConstraintType.A){
			RaceMemUNode node = (RaceMemUNode) this.var1;
			RaceLockUNode lockNode = (RaceLockUNode) this.var2;
			builder.append("lock(" + node.getSig() + ") = " + lockNode.getSig() + " ; score : " + score);
			builder.append(" ; type : A");
		}
		else{
			RaceMemUNode node1 = (RaceMemUNode) this.var1;
			RaceMemUNode node2 = (RaceMemUNode) this.var2;
			builder.append("lock(" + node1.getSig() + ") = lock(" + node2.getSig() + ")" + " ; score : " + score);
		}
		return builder.toString();
	}
	
	
	
	
}
