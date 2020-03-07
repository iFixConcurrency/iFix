//package edu.tamu.aser.tide.views.fix;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Set;
//
//import org.eclipse.jface.action.Action;
//import org.eclipse.jface.action.MenuManager;
//import org.eclipse.jface.viewers.TreeViewer;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Menu;
//import org.eclipse.swt.widgets.Text;
//import org.eclipse.ui.part.ViewPart;
//
//import edu.tamu.aser.tide.engine.TIDEEngine;
//import edu.tamu.aser.tide.engine.TIDERace;
//import edu.tamu.aser.tide.views.BugContentProvider;
//import ann.iDebugger.nodes.LockingPolicy;
//import ann.iDebugger.nodes.RepairPolicy;
//
//public class PFixView extends ViewPart {
//
//	protected TreeViewer treeViewer;
//	protected Text text;
//	protected PFixBugLabelProvider labelProvider;
//	protected PFixDetail pFixDetail;
//	protected Action jumpToLineInEditor;
//
//	protected TIDEEngine bugEngine;
//
//	public PFixView() {
//		super();
//	}
//
//	public void setEngine(TIDEEngine bugEngine) {
//		this.bugEngine = bugEngine;
//	}
//
//	@Override
//	public void setFocus() {
//
//	}
//
//	@Override
//	public void createPartControl(Composite parent) {
//		/*
//		 * Create a grid layout object so the text and treeviewer are layed out
//		 * the way I want.
//		 */
//		GridLayout layout = new GridLayout();
//		layout.numColumns = 1;
//		layout.verticalSpacing = 2;
//		layout.marginWidth = 0;
//		layout.marginHeight = 2;
//		parent.setLayout(layout);
//
//		// Create the tree viewer as a child of the composite parent
//		treeViewer = new TreeViewer(parent);
//		treeViewer.setContentProvider(new BugContentProvider());
//		labelProvider = new PFixBugLabelProvider();
//		treeViewer.setLabelProvider(labelProvider);
//		treeViewer.setUseHashlookup(true);
//
//		// layout the tree viewer below the text field
//		GridData layoutData = new GridData();
//		layoutData.grabExcessHorizontalSpace = true;
//		layoutData.grabExcessVerticalSpace = true;
//		layoutData.horizontalAlignment = GridData.FILL;
//		layoutData.verticalAlignment = GridData.FILL;
//		treeViewer.getControl().setLayoutData(layoutData);
//
//
//		initializeTree();
//
//		// create a context menu in the view
//		MenuManager manager = new MenuManager();
//		Control control = treeViewer.getControl();
//		Menu menu = manager.createContextMenu(control);
//		control.setMenu(menu);
//		getSite().registerContextMenu("edu.tamu.aser.tide.views.fixmenu", manager, treeViewer);
//	}
//
//	private void initializeTree() {
//		// initialize the tree
//		pFixDetail = new PFixDetail(null);
//	}
//
//
//	public void initialGUI() {
//		// clear all
//		pFixDetail.clear();
//		// refresh
//		treeViewer.refresh();
//		translateToInput2();
//		treeViewer.setInput(pFixDetail);
//		Set<TIDERace> bugs = new HashSet<TIDERace>();
//		treeViewer.expandToLevel(pFixDetail, 1);
//	}
//
//	private void translateToInput2() {
//		RepairPolicy policy = RepairPolicy.getInstance();
//		ArrayList<LockingPolicy> list = new ArrayList<>(policy.getLockingPolicies());
//		Collections.sort(list);
//		for(int i = 0; i < list.size(); i ++){
//			pFixDetail.createChild(list.get(i), i);
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	public void updateGUI(HashSet<TIDERace> addedbugs, HashSet<TIDERace> removedbugs) {
//
//	}
//
//	public void choose(PFixSubNode node) {
//		System.out.println("choose");
////		node.setPolicy();
//	}
//
//	public void considerBugs(HashSet<TIDERace> considerbugs) {
//
//	}
//
//	public void ignoreBugs(HashSet<TIDERace> removedbugs){
//
//	}
//}
