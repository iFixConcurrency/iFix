//package edu.tamu.aser.tide.plugin.handlers;
//
//import java.util.HashSet;
//
//import org.eclipse.core.commands.AbstractHandler;
//import org.eclipse.core.commands.ExecutionEvent;
//import org.eclipse.core.commands.ExecutionException;
//import org.eclipse.core.commands.common.NotDefinedException;
////import org.eclipse.jface.viewers.ISelection;
////import org.eclipse.jface.viewers.TreeSelection;
////import org.eclipse.ui.handlers.HandlerUtil;
//
//import edu.tamu.aser.tide.engine.TIDEEngine;
//import edu.tamu.aser.tide.engine.TIDERace;
//import edu.tamu.aser.tide.views.fix.PFixNode;
//import edu.tamu.aser.tide.views.fix.PFixSubNode;
//import edu.tamu.aser.tide.views.fix.PFixView;
//import ann.iDebugger.handlers.FixHub;
//import ann.iDebugger.nodes.RepairPolicy;
//
///**
// * do not consider this variable with this sig any more, as well as all other
// * bugs involving this sig and variable.
// *
// *
// */
//public class IgnoreLockHandler extends AbstractHandler {
//
//	// set the view to remove selected bugs
//	public ConvertHandler cHandler;
//	public PFixView pFixView;
//	public TIDEEngine engine;
//	public FixHub fixHub;
//
//	public IgnoreLockHandler() {
//		super();
//		cHandler = edu.tamu.aser.tide.plugin.Activator.getDefault().getConvertHandler();
//		fixHub = FixHub.getInstance();
//	}
//
//	@Override
//	public Object execute(ExecutionEvent event) throws ExecutionException {
//		System.out.println("here");
//		boolean ignore = true;
//		String command = "";
//		try {
//			command = event.getCommand().getName();
//		} catch (NotDefinedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		if (cHandler == null) {
//			cHandler = edu.tamu.aser.tide.plugin.Activator.getDefault().getConvertHandler();
//			if (cHandler == null)
//				return null;
//		}
////		pFixView = cHandler.getPFixView();
//		engine = cHandler.getCurrentModel().getBugEngine();
//		// get the node
//		ISelection sel = HandlerUtil.getActiveMenuSelection(event);
//		TreeSelection treeSel = (TreeSelection) sel;
//		// ifile:
////		TreePath path = treeSel.getPaths()[0];
////		IPath ipath = ((SourceType) path.getSegment(0)).getParent().getPath();
////		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(ipath);
//		Object element = treeSel.getFirstElement();
//		HashSet<TIDERace> all = new HashSet<>();
//		if (element instanceof PFixNode) {
//
//			PFixNode node = (PFixNode) element;
//
//			System.out.println("choose locking policy " + node.getLockingPolicy());
//			RepairPolicy repairPolicy = RepairPolicy.getInstance();
//			repairPolicy.chooseLockingPolicy(node.getLockingPolicy());
////			pFixView.choose(node);
//		}
//		// remove the marker from editor
////		cHandler.getCurrentModel().removeBugMarkersForIgnore(all);
//		// remove the bug from view
////		pFixView.choose(all);
//		return null;
//	}
//
//}
