package fix.iDebugger.handlers;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.ui.IWorkbench;
//import org.eclipse.ui.IWorkbenchPage;
//import org.eclipse.ui.IWorkbenchWindow;
//import org.eclipse.ui.PartInitException;
//import org.eclipse.ui.PlatformUI;

import com.ibm.wala.util.collections.Pair;

import edu.tamu.aser.tide.engine.TIDEEngine;
import edu.tamu.aser.tide.engine.TIDERace;
import edu.tamu.aser.tide.marker.BugMarker;
import edu.tamu.aser.tide.nodes.DLockNode;
import edu.tamu.aser.tide.nodes.INode;
import edu.tamu.aser.tide.nodes.LockPair;
import edu.tamu.aser.tide.nodes.MemNode;
import edu.tamu.aser.tide.plugin.handlers.ConvertHandler;
import edu.tamu.aser.tide.shb.SHBGraph;
import edu.tamu.aser.tide.shb.Trace;
//import edu.tamu.aser.tide.views.fix.PFixView;
import fix.iDebugger.nodes.RaceLockUNode;
import fix.iDebugger.nodes.RaceMemUNode;
import fix.iDebugger.nodes.RepairPolicy;

public class FixHub {
	
	public static boolean DEBUG = false;
	
	// to get bug engine from d4
	private ConvertHandler convertHandler;
	
	public HashSet<String> sharedFields;
	// 记录每个变量的加锁次数
	public HashMap<HashSet<String>, HashMap<HashSet<String>, Integer>> sharedFieldsToLocks;
	// 根据lockSig找到lock
	public HashMap<HashSet<String>, HashSet<DLockNode>> sigToLocks;
	public HashMap<HashSet<String>, HashSet<RepairPolicy>> sigToRepairPolicy;
	// 记录每个node被哪些lock加锁了
	public HashMap<MemNode, HashSet<DLockNode>> nodeToLocks;
	public HashMap<DLockNode, HashSet<MemNode>> lockToNodes;
	// 修复建议
	public HashSet<Pair<MemNode, HashSet<DLockNode>>> repairSuggestion;
	// 新锁名称
	public HashMap<HashSet<String>, String> newLockMap;
	public int newLockIndex = 0;
	public HashMap<MemNode, RepairPolicy> repairPolicyMap;
	
	// marker -> node
	public HashMap<Long, MemNode> markerNodeMap;
	
	public HashMap<MemNode, IMarker> nodeToMarkerMap;
	
	private List<String> markFixLocationList = new ArrayList<String>();

	private static FixHub fixHub = new FixHub();
	
//	public PFixView pFixView;
	
	public TIDEEngine engine;
	
	public static long d4DetectionTime;
	public static long policyConjectureTime;
	public static long applyFixTime;
	
	public static List<Long> d4DetectionTimeList;
	public static List<Long> policyConjectureTimeList;
	public static List<Long> applyFixTimeList;
	public static boolean firstTime = true;
	
	public static int lockUsed;
	public static int raceCount;
	
	private FixHub(){
		sharedFields = new HashSet<String>();
		sharedFieldsToLocks = new HashMap<HashSet<String>, HashMap<HashSet<String>, Integer>>();
		sigToLocks = new HashMap<>();
		sigToRepairPolicy = new HashMap<>();
		nodeToLocks = new HashMap<>();
		lockToNodes = new HashMap<>();
		repairSuggestion = new HashSet<>();
		newLockMap = new HashMap<>();
		newLockIndex = 0;
		repairPolicyMap = new HashMap<>();
		repairPolicyMap = new HashMap<>();
		markerNodeMap = new HashMap<>();
		nodeToMarkerMap = new HashMap<>();
		
		d4DetectionTimeList = new ArrayList<>();
		policyConjectureTimeList = new ArrayList<>();
		applyFixTimeList = new ArrayList<>();
	}
	
	public void clear(){
		sharedFields.clear();
		sharedFieldsToLocks.clear();
		sigToLocks.clear();
		sigToRepairPolicy.clear();
		nodeToLocks.clear();
		lockToNodes.clear();
		repairSuggestion.clear();
		newLockMap.clear();
		newLockIndex = 0;
		repairPolicyMap.clear();
		repairPolicyMap.clear();
		markerNodeMap.clear();
		nodeToMarkerMap.clear();
		markFixLocationList.clear();
		RaceLockUNode.clear();
		RaceMemUNode.clear();
		RepairPolicy.getInstance().clear();
		lockUsed = 0;
	
	}
	
	public static FixHub getInstance(){
		return fixHub;
	}
	
	/**
	 * handle fix action for iDebugger
	 */
	public boolean handleFix() {
		// take race info and trace info
		engine = TIDEEngine.getEngine();
		
		if(engine == null){
			return false;
		}
		SHBGraph shb = engine.shb;
		if(shb == null){
			System.err.println("NO BUG TO HANDLE");
			return false;
		}
		
		ArrayList<Trace> traces = shb.getAllTraces();
		
		// count the times each lock protect a share variable
		for (Trace item : traces) {
			// lock pair indicates a sync block
			if (item.getLockPair() != null && item.getLockPair().size() > 0) {
				
				ArrayList<INode> nodes = item.getNodes();
				// System.out.println(nodes);
				int i = 0;
				for (LockPair lockPair : item.getLockPair()) {
					
					// judge the lock pair has only protected a method. 
					boolean flag = true;
					
					while (i < nodes.size() && !nodes.get(i).equals(lockPair.lock)) {
						i++;
					}
					i++;
					while (i < nodes.size() && !nodes.get(i).equals(lockPair.unlock)) {
						INode node = nodes.get(i);
						if (node instanceof MemNode) {
							if(flag){
								// record lock node
								RaceLockUNode.getNode(lockPair.lock).addLockNode(lockPair.lock);
								
								// record lock info 
								if (!lockToNodes.containsKey(lockPair.lock)) {
									lockToNodes.put(lockPair.lock, new HashSet<>());
								}
							}
							flag = false;
							MemNode mnode = (MemNode) node;
							lockToNodes.get(lockPair.lock).add(mnode);
							if (nodeToLocks.containsKey(mnode)) {
								nodeToLocks.get(mnode).add(lockPair.lock);
							} else {
								nodeToLocks.put(mnode, new HashSet<>());
								nodeToLocks.get(mnode).add(lockPair.lock);
							}
						}
						i++;
					}
					
					i++;
				}
			}
		}
		// for repair suggestion

		// record all shared variables and available locks
		
		raceCount = engine.races.size();
		for (TIDERace race : engine.races) {
			RaceMemUNode.getNode(race.node1).addMemNode(race.node1);
			RaceMemUNode.getNode(race.node2).addMemNode(race.node2);
			RaceMemUNode.getNode(race.node1).setRace(race.node1, race.node2);
			RaceMemUNode.getNode(race.node2).setRace(race.node1, race.node2);
		}
		
		for(MemNode tNode : nodeToLocks.keySet()){
			RaceMemUNode.getNode2(tNode).addMemNode(tNode);
		}
		
		// optimize node, make sure every share variable only appears once
		RaceMemUNode.optimize();
		RaceLockUNode.optimize();
		RaceLockUNode.generateStaticLock();
		
		// count times
		for(DLockNode lockNode : lockToNodes.keySet()){
			for(MemNode memNode : lockToNodes.get(lockNode)){
				if(RaceMemUNode.hasNode(memNode)){
					RaceMemUNode memUNode = RaceMemUNode.getNode(memNode);
					memUNode.addLock(RaceLockUNode.getNode(lockNode));
				}
			}
		}
		
		// get instance of locking policy
		RepairPolicy policy = RepairPolicy.getInstance();
		policy.init();
		if(DEBUG){
			policy.showNodes();
		}
		
		// TODO if no solution, static lock is generated
		// add constraint of lock(a) = lock(b)
		for(DLockNode lockNode : lockToNodes.keySet()){
			HashSet<MemNode> nodes = lockToNodes.get(lockNode);
			List <MemNode> nodesList = new ArrayList<>();
			for(MemNode tNode : nodes){
				if(RaceMemUNode.hasNode(tNode)){
					nodesList.add(tNode);
				}
			}
			for(int i = 0; i < nodesList.size(); i ++){
				for(int j = i + 1; j < nodesList.size(); j ++){
					if(RaceMemUNode.getNode(nodesList.get(i)).equals(RaceMemUNode.getNode(nodesList.get(j)))){
						continue;
					}
					policy.constraint.addItemB(RaceMemUNode.getNode(nodesList.get(i)), RaceMemUNode.getNode(nodesList.get(j)), 1);
				}
			}
		}


		policy.generateLockingPolicy();
		System.out.println("lock:" + policy.repository.getLockList().size() + ", var: " + policy.repository.getVariableList().size());
		policy.showBoundEffectiveness();

		if(DEBUG){
			System.out.println("fixHub prepared");
		}
		
		// for debug only
		HashSet<String> varSet = new HashSet<>();
		for(TIDERace race : engine.races){
			String s = race.node1.prefix;
			if(s.contains("array")){
				varSet.add(race.node1.getLocalSig());
			}
			else{
				varSet.add(s);
			}
			s = race.node2.getPrefix();
			if(s.contains("array")){
				varSet.add(race.node2.getLocalSig());
			}
			else{
				varSet.add(s);
			}
		}
		
//		System.err.println("Variable count : " + varSet.size());
		List<String> varList = new ArrayList<>(varSet);
		java.util.Collections.sort(varList);
		
//		for(String string : varList){
//			System.err.println(string);
//		}
		
		if(DEBUG){
			policy.showPolicy();
		}
		
		return true;
	}
	
	public void addMarker(IJavaProject project, IFile file){
		//full path of the project
		IPath fullPath = file.getProject().getFullPath();
		RepairPolicy policy = RepairPolicy.getInstance();
		try{
			for(RaceMemUNode node : policy.repository.getVariableList()) {
				if( node.getRaceMap() != null && node.getRaceMap().size() > 0){
					showRepairSuggestion(fullPath, node);
				}
			}
		}
		catch(CoreException e){
			System.err.println("error found when adding marker");
			e.printStackTrace();
		}

	}
	
	private void showRepairSuggestion(IPath fullPath, RaceMemUNode memUNode) throws CoreException {
		if(memUNode == null){
			System.err.println("error: repair suggestion to node not generate");
			return;
		}
		
		for(MemNode raceNode : memUNode.getAllNodes()){			
			String raceMsg = "";
			String markLocation = raceNode.getFile().toString() + raceNode.getLine();
			
			String sig = memUNode.getRaceMsg(raceNode);
			raceMsg += "Race data: "+ sig;
			
			//use list to make sure don't create marker repeatedly
			if(!markFixLocationList.contains(markLocation)) {
				IMarker marker = null;
				IFile file = raceNode.getFile();
				
//				marker = createMarkerRace(file, node.getLine(), repairMsg);
				
				//create fix marker
				if(DEBUG){
					System.out.println("add mark on " + raceNode);
				}
				
				marker = createMarkerFix(file, raceNode.getLine(), raceMsg);

				HashSet<IMarker> newMarkers = new HashSet<>();
				newMarkers.add(marker);
				markerNodeMap.put(marker.getId(), raceNode);
				nodeToMarkerMap.put(raceNode, marker);
//				bug_marker_map.put(raceMsg, newMarkers);
				
				//record it
				markFixLocationList.add(markLocation);
			}
		}

	}

	
	private IMarker createMarkerFix(IFile file, int line, String msg) throws CoreException {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(IMarker.LINE_NUMBER, line); 
		attributes.put(IMarker.MESSAGE, msg);
		IMarker newMarker = file.createMarker(BugMarker.TYPE_TROUBLING);
		newMarker.setAttributes(attributes);
		IMarker[] problems = file.findMarkers(BugMarker.TYPE_TROUBLING,true,IResource.DEPTH_INFINITE);
		return newMarker;
	}
	
	protected void justLockIt(MemNode node, DLockNode lockNode) {
		String string = null;
		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(lockNode.file.getLocation().toFile()));
			while ((string = reader.readLine()) != null) {
				if(reader.getLineNumber() == lockNode.getLine()) {
					break;
				}
			}
			reader.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		String pattern = ".*synchronized\\s*\\((.*)\\).*";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(string);
		if(m.find()) {
			string = m.group(1);
		}
		else {
			System.err.println("error found : " + string + " has not lock data");
		}
		if(DEBUG){
			System.out.println("use lock: " + string + " to lock it");
		}
		
	}

//	public void initialGUI(){
//		new Thread(new Runnable() {
//			public void run() {
//				while (true) {
//					try { Thread.sleep(100);} catch (Exception e) {e.printStackTrace();}
//					Display.getDefault().asyncExec(new Runnable() {
//						public void run() {
//							//do update
//							try {
//								IWorkbench workbench = PlatformUI.getWorkbench();
//								IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
//								IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
//								pFixView = (PFixView) workbenchPage.showView("edu.tamu.aser.tide.views.fix.pfixview");
//								pFixView.initialGUI();
//							} catch (PartInitException e) {
//								// TODO Auto-generated catch block
//								System.err.println("iDebugger view initial failed");
//								e.printStackTrace();
//							}
//						}
//					});
//					break;
//				}
//			}
//		}).start();
//	}
	
	public void removeMarkers(){
		RepairPolicy repairPolicy = RepairPolicy.getInstance();
		
		for(MemNode memNode : nodeToMarkerMap.keySet()){
			try{
				IMarker marker = nodeToMarkerMap.get(memNode);
				marker.delete();
			}
			catch(CoreException e){
				System.err.println("marker deleted failed");
				e.printStackTrace();
			}
		}
	}
}
