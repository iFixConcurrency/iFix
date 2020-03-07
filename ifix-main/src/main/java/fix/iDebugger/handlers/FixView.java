//package ann.iDebugger.handlers;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IMarker;
//import org.eclipse.core.resources.IResource;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.ui.PartInitException;
//import org.eclipse.ui.PlatformUI;
//
//import edu.tamu.aser.tide.marker.BugMarker;
//import edu.tamu.aser.tide.views.fix.PFixView;
//
//public class FixView {
//	private PFixView pFixView;
//	private FixHub fixHub;
//
//	private static FixView fixView;
//
//	public static FixView getInstence(){
//		if(fixView == null){
//			fixView = new FixView();
//		}
//		return fixView;
//	}
//
//	private FixView(){
//		fixHub = FixHub.getInstance();
//		try {
//			pFixView = (PFixView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
//					.getActivePage().showView("edu.tamu.aser.tide.views.fix.pfixview");
//		} catch (PartInitException e) {
//			System.out.println("i debugger fix view initial failed");
//			e.printStackTrace();
//		}
//	}
//
//	public void update(){
//		new Thread(new Runnable() {
//			public void run() {
//				while (true) {
//					try { Thread.sleep(20);} catch (Exception e) {System.err.println(e);}
//					Display.getDefault().asyncExec(new Runnable() {
//						public void run() {
//							pFixView.initialGUI();
//						}
//					});
//					break;
//				}
//			}
//		}).start();
//	}
//
//
//
//
//
//	private IMarker createMarkerFix(IFile file, int line, String msg) throws CoreException {
//		Map<String, Object> attributes = new HashMap<String, Object>();
//		attributes.put(IMarker.LINE_NUMBER, line);
//		attributes.put(IMarker.MESSAGE, msg);
//		IMarker newMarker = file.createMarker(BugMarker.TYPE_TROUBLING);
//		newMarker.setAttributes(attributes);
//		IMarker[] problems = file.findMarkers(BugMarker.TYPE_TROUBLING,true,IResource.DEPTH_INFINITE);
//		return newMarker;
//	}
//}
