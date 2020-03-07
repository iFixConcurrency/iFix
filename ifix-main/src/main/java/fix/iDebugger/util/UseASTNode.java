package fix.iDebugger.util;

public class UseASTNode {

	int start;
	int end;

	public UseASTNode() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UseASTNode(int start, int end) {
		super();
		this.start = start;
		this.end = end;
	}

	public boolean inNodeStartOrEnd(int loc) {
		if (loc == start || loc == end)
			return true;
		else
			return false;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
	
}
