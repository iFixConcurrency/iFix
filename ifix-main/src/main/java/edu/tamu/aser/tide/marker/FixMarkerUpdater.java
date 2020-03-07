//package edu.tamu.aser.tide.marker;
//
//import org.eclipse.core.resources.IMarker;
//import org.eclipse.jface.text.IDocument;
//import org.eclipse.jface.text.Position;
//import org.eclipse.ui.texteditor.IMarkerUpdater;
//
//public class FixMarkerUpdater implements IMarkerUpdater{
//
//	@Override
//	public String[] getAttribute() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getMarkerType() {
//		// TODO Auto-generated method stub
//		return "edu.umd.cs.findbugs.plugin.eclipse.findbugsMarkerTroubling";
//	}
//
//	@Override
//	public boolean updateMarker(IMarker marker, IDocument doc, Position line) {
//		if(marker instanceof BugMarker){
//			BugMarker bugMarker = (BugMarker) marker;
////			return true;
//		}
////		return false;
//		return true;
//	}
//
//}
