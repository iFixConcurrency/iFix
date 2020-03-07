//package edu.tamu.aser.tide.views.fix;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//
//import edu.tamu.aser.tide.views.ITreeNode;
//
//public abstract class PFixTreeNode implements ITreeNode{
//	protected PFixTreeNode parent;
//	protected boolean isNewest = false;
//	protected ArrayList children = new ArrayList<>();
//
//	public PFixTreeNode(PFixTreeNode parent) {
//		this.parent = parent;
//	}
//
//	public boolean hasChildren() {
//		return children.size() > 0;
//	}
//
//	public ITreeNode getParent() {
//		return parent;
//	}
//
//	public ArrayList getChildren() {
//		return children;
//	}
//
//
//	public boolean isNewest() {
//		return this.isNewest;
//	}
//
//	/* subclasses should override this method and add the child nodes */
//	protected abstract void createChildren(ArrayList<LinkedList<String>> trace, String fix);
//
//}