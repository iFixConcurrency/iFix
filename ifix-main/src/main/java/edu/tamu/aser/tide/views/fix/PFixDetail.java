//package edu.tamu.aser.tide.views.fix;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//
//import org.eclipse.jface.resource.ImageDescriptor;
//
//import edu.tamu.aser.tide.engine.TIDERace;
//import edu.tamu.aser.tide.nodes.MemNode;
//import edu.tamu.aser.tide.plugin.Activator;
//import ann.iDebugger.nodes.LockingPolicy;
//import ann.iDebugger.nodes.RepairPolicy;
//
//public class PFixDetail extends PFixTreeNode {
//	protected String name;
//	protected HashMap<String, PFixNode> map = new HashMap<>();
//
//	private List<String> nodeInfoList = new ArrayList<String>();
//
//	public PFixDetail(PFixTreeNode parent) {
//		super(parent);
//		this.name = "Race Detail";
//	}
//
//	@Override
//	public String getName() {
//		return name;
//	}
//
//	@Override
//	public ImageDescriptor getImage() {
//		return Activator.getImageDescriptor("folder_icon.gif");
//	}
//
//	@Override
//	protected void createChildren(ArrayList<LinkedList<String>> trace, String fix) {
//
//	}
//
//	public void clear() {
//		super.children.clear();
//		map.clear();
//
//		nodeInfoList.clear();
//	}
//
//	public void createChild(LockingPolicy key, int id) {
//		createChild(key, id, false);
//
//	}
//
//	@SuppressWarnings("unchecked")
//	private void createChild(LockingPolicy key, int id, boolean isNewest) {
//		// if current race node has been protected by suggestion lock,we don't
//		// create it.
//		// TODO
//
//		PFixNode node = new PFixNode(this, key, id, isNewest);
//		super.children.add(node);
//	}
//
//}
