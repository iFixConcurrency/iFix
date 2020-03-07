//package edu.tamu.aser.tide.views.fix;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Queue;
//
//import org.eclipse.jface.resource.ImageDescriptor;
//
//import com.ibm.wala.util.collections.Pair;
//
//import edu.tamu.aser.tide.engine.TIDERace;
//import edu.tamu.aser.tide.nodes.MemNode;
//import edu.tamu.aser.tide.plugin.Activator;
//import ann.iDebugger.nodes.LockingPolicy;
//import ann.iDebugger.nodes.RaceLockUNode;
//import ann.iDebugger.nodes.RaceMemUNode;
//import ann.iDebugger.nodes.RepairNode;
//import ann.iDebugger.nodes.RepairPolicy;
//
//public class PFixNode extends PFixTreeNode {
//	protected String name;
//	protected LockingPolicy lockingPolicy;
//	protected int id;
//
//	public PFixNode(PFixTreeNode parent) {
//		this(parent, false);
//	}
//
//	public PFixNode(PFixTreeNode parent, boolean isNewest) {
//		super(parent);
//		this.isNewest = isNewest;
//		initialNode();
//	}
//
//	public PFixNode(PFixTreeNode parent, LockingPolicy lockingPolicy, int id, boolean isNewest) {
//		super(parent);
//		this.isNewest = isNewest;
//		this.lockingPolicy = lockingPolicy;
//		this.id = id;
//		initialNode();
//	}
//
//	private void initialNode() {
//		/*if (repairPolicy.getSuggestionLock().isNew()) {
//			name = "Use new static object " + repairPolicy.getSuggestionLock().getLockName() + " to protect "
//					+ repairPolicy.getRaceNode().getPrefix();
//		} else {
//			name = "Use object " + repairPolicy.getSuggestionLock().getLockName() + " to protect "
//					+ repairPolicy.getRaceNode().getPrefix();
//		}*/
//		name = "use locking policy " + (this.id + 1) + " with " + lockingPolicy.getScore() + " constraints satisfied";
//		createChildren();
//	}
//
//
//	private void createChildren() {
//		if (this instanceof PFixNode) {
//			for(Pair<RaceMemUNode, RaceLockUNode> pair : lockingPolicy.getRepairList()){
//				PFixSubNode pFixSubNode = new PFixSubNode(this, pair.fst, pair.snd.getRepairNode(), lockingPolicy.getScore());
//				super.children.add(pFixSubNode);
//			}
//		}
//	}
//
//	@Override
//	public String getName() {
//		return name;
//	}
//
//	@Override
//	public ImageDescriptor getImage() {
//		return Activator.getImageDescriptor("annotationsView.png");
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	protected void createChildren(ArrayList<LinkedList<String>> trace, String fix) {
//
//	}
//
//	/*public void removeMostUseLock() {
//		repairPolicy.removeMostUsedLocks();
//		repairPolicy.generateSuggestionLock();
//		if (repairPolicy.getSuggestionLock() == null) {
//			repairPolicy.generateStaticLock();
//		}
//	}
//
//	public void update() {
//		if (repairPolicy.getSuggestionLock().isNew()) {
//			name = "Use new static object " + repairPolicy.getSuggestionLock().getLockName() + " to protect "
//					+ repairPolicy.getRaceNode().getPrefix();
//		} else {
//			name = "Use object " + repairPolicy.getSuggestionLock().getLockName() + " to protect "
//					+ repairPolicy.getRaceNode().getPrefix();
//		}
//	}*/
//
//	@Override
//	public String toString() {
//		return name + "\n" + lockingPolicy.toString();
//	}
//
//	public LockingPolicy getLockingPolicy() {
//		return lockingPolicy;
//	}
//
//	public void setLockingPolicy(LockingPolicy lockingPolicy) {
//		this.lockingPolicy = lockingPolicy;
//	}
//
//
//
//}
