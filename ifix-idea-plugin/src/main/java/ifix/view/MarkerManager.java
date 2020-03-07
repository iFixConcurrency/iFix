package ifix.view;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import edu.tamu.aser.tide.nodes.MemNode;
import fix.iDebugger.nodes.LockingPolicy;
import fix.iDebugger.nodes.RaceMemUNode;
import fix.iDebugger.nodes.RepairPolicy;

import java.util.*;

/**
 * @author ann
 */
public class MarkerManager {

    /**
     * filename -> set(line number)
     */
    private Map<String, Set<Integer>> markerMap;
    private Set<String> markerSet;

    private String message = "";

    private MarkerManager() {
        this.markerMap = new HashMap<>();
        this.markerSet = new HashSet<>();
        RepairPolicy policy = RepairPolicy.getInstance();
        Queue<LockingPolicy> queue = policy.getLockingPolicies();
        for(LockingPolicy p : queue){


        }
    }

    public static MarkerManager manager = new MarkerManager();

    public static MarkerManager getInstance(){
        return manager;
    }

    public void generateMarker(){

        RepairPolicy policy = RepairPolicy.getInstance();

        for(RaceMemUNode rNode : policy.repository.getVariableList()){
            for(MemNode memNode : rNode.getAllNodes()){
                this.markerMap.putIfAbsent(memNode.filePath, new HashSet<>());
                this.markerMap.computeIfPresent(memNode.filePath, (path, set) -> {
                    set.add(memNode.getLine());
                    return set;
                });
            }
        }
    }

    public boolean shouldMark(PsiElement element){
        TextRange range = element.getTextRange();
        String path = element.getContainingFile().getVirtualFile().getPath();
        Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());
        int lastLine = -1;
        if(this.markerMap.containsKey(path)){
            Set<Integer> lineSet = this.markerMap.get(path);
            for(Integer line : lineSet){
                lastLine = Math.max(line, lastLine);
                TextRange lineRange = new TextRange(document.getLineStartOffset(line - 1), document.getLineEndOffset(line - 1));
                if(lineRange.contains(range)){
                    if(!this.markerSet.contains(path + ":" + line)){
                        this.markerSet.add(path + ":" + line);
                        return true;
                    }
                    return false;
                }
            }
        }
        if(document.getLineNumber(range.getStartOffset()) >= lastLine){
            this.markerSet.clear();
        }
        return false;
    }

    public String getMarkerMessage(){
        return this.message;
    }

    public void removeMarkers(){
        List<VirtualFile> list = new ArrayList<>();
        for(String file : markerMap.keySet()){
            VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(file);
            if(vf != null){
                list.add(vf);
            }
        }
        System.out.println(list.size());
        this.markerMap.clear();
        this.markerSet.clear();
        for(VirtualFile vf : list){
            vf.refresh(true, true);
//            System.out.println(vf.getPath() + "  refreshed");
        }
    }

//
//    private void showRepairSuggestion(String fullPath, RaceMemUNode memUNode) {
//        if(memUNode == null){
//            System.err.println("error: repair suggestion to node not generate");
//            return;
//        }
//
//        for(MemNode raceNode : memUNode.getAllNodes()){
//            String raceMsg = "";
//            String markLocation = raceNode.getFile().toString() + raceNode.getLine();
//
//            String sig = memUNode.getRaceMsg(raceNode);
//            raceMsg += "Race data: "+ sig;
//
//            //use list to make sure don't create marker repeatedly
//            if(!markFixLocationList.contains(markLocation)) {
//                IMarker marker = null;
//                IFile file = raceNode.getFile();
//
////				marker = createMarkerRace(file, node.getLine(), repairMsg);
//
//                //create fix marker
//                if(DEBUG){
//                    System.out.println("add mark on " + raceNode);
//                }
//
//                marker = createMarkerFix(file, raceNode.getLine(), raceMsg);
//
//                HashSet<IMarker> newMarkers = new HashSet<>();
//                newMarkers.add(marker);
//                markerNodeMap.put(marker.getId(), raceNode);
//                nodeToMarkerMap.put(raceNode, marker);
////				bug_marker_map.put(raceMsg, newMarkers);
//
//                //record it
//                markFixLocationList.add(markLocation);
//            }
//        }
//
//    }
//
//
//    private LineMarkerInfo createMarkerFix(IFile file, int line, String msg) {
//
//
//
//        Map<String, Object> attributes = new HashMap<String, Object>();
//        attributes.put(IMarker.LINE_NUMBER, line);
//        attributes.put(IMarker.MESSAGE, msg);
//        IMarker newMarker = file.createMarker(BugMarker.TYPE_TROUBLING);
//        newMarker.setAttributes(attributes);
//        IMarker[] problems = file.findMarkers(BugMarker.TYPE_TROUBLING,true, IResource.DEPTH_INFINITE);
//        return newMarker;
//    }
}
