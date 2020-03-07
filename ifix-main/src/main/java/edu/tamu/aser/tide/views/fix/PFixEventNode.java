////package edu.tamu.aser.tide.views.fix;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//
//import org.eclipse.jface.resource.ImageDescriptor;
//
//import edu.tamu.aser.tide.plugin.Activator;
//
//public class PFixEventNode extends PFixTreeNode{
//	protected String name;
//
//	public PFixEventNode(PFixTreeNode parent, String event) {
//		super(parent);
//		this.name = event;
//	}
//
//	@Override
//	public String getName() {
//		return name;
//	}
//
//	@Override
//	public ImageDescriptor getImage() {
//		return Activator.getImageDescriptor("forward_nav.gif");
//	}
//
//	@Override
//	protected void createChildren(ArrayList<LinkedList<String>> trace, String fix) {
//		// no child
//	}
//
//}
