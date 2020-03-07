///*
// * Contributions to FindBugs
// * Copyright (C) 2006, Institut for Software
// * An Institut of the University of Applied Sciences Rapperswil
// *
// * Author: Thierry Wyss, Marco Busarello
// *
// * This library is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public
// * License as published by the Free Software Foundation; either
// * version 2.1 of the License, or (at your option) any later version.
// *
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public
// * License along with this library; if not, write to the Free Software
// * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// */
//package edu.tamu.aser.tide.engine;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Queue;
//
//import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IMarker;
//import org.eclipse.jface.text.BadLocationException;
//import org.eclipse.jface.text.IDocument;
//import org.eclipse.jface.text.IRegion;
////import org.eclipse.ui.IMarkerResolution;
////import org.eclipse.ui.IMarkerResolutionGenerator2;
//
//import com.ibm.wala.classLoader.IBytecodeMethod;
//import com.ibm.wala.classLoader.IMethod.SourcePosition;
//import com.ibm.wala.ssa.SSAInstruction;
//import com.ibm.wala.util.collections.Pair;
//
//import edu.tamu.aser.tide.nodes.DLockNode;
//import edu.tamu.aser.tide.nodes.MemNode;
//import ann.iDebugger.handlers.FixHub;
//import ann.iDebugger.nodes.LockingPolicy;
//import ann.iDebugger.nodes.RaceLockUNode;
//import ann.iDebugger.nodes.RaceMemUNode;
//import ann.iDebugger.nodes.RepairNode;
//import ann.iDebugger.nodes.RepairPolicy;
//import ann.iDebugger.util.UseAST;
//import scala.annotation.varargs;
//
//
///**
// * The <CODE>BugResolutionGenerator</CODE> searchs for bug-resolutions, that can
// * be used to fix the specific bug-type.
// *
// * @author <a href="mailto:twyss@hsr.ch">Thierry Wyss</a>
// * @author <a href="mailto:mbusarel@hsr.ch">Marco Busarello</a>
// * @author <a href="mailto:g1zgragg@hsr.ch">Guido Zgraggen</a>
// */
//public class BugResolutionGenerator implements IMarkerResolutionGenerator2 {
//
//	private boolean bugResolutionsLoaded;
//
//	@Override
//	public IMarkerResolution[] getResolutions(IMarker marker) {
//		IMarkerResolution[] resolutions = new IMarkerResolution[1];
//
//		IMarkerResolution resolution = new IMarkerResolution() {
//
//			@Override
//			public void run(IMarker marker) {
//				long startTime = System.currentTimeMillis();
//				RepairPolicy policy = RepairPolicy.getInstance();
//				policy.applyLockingPolicy();
//				FixHub.applyFixTime = System.currentTimeMillis() - startTime;
//				System.err.println("policy apply cost :" + FixHub.applyFixTime);
//
//
//				if(FixHub.firstTime){
//					FixHub.firstTime = false;
//				}
//				else{
//					FixHub.d4DetectionTimeList.add(FixHub.d4DetectionTime);
//					FixHub.policyConjectureTimeList.add(FixHub.policyConjectureTime);
//					FixHub.applyFixTimeList.add(FixHub.applyFixTime);
//					if(FixHub.applyFixTimeList.size() == 5){
//						System.err.println("==================EACH=================");
//						long a = 0, b = 0, c = 0;
//						for(int i = 0; i < 5 ; i ++){
//							System.err.println(i + "	" + FixHub.d4DetectionTimeList.get(i) + "	"
//						+ FixHub.policyConjectureTimeList.get(i) + "	" + FixHub.applyFixTimeList.get(i));
//							c += FixHub.applyFixTimeList.get(i);
//							a += FixHub.d4DetectionTimeList.get(i);
//						    b += FixHub.policyConjectureTimeList.get(i);
//						}
//						System.err.println("==================AVG==================");
//						System.err.println("avg	" + a * 1.0 / 5 + "	" + b * 1.0 / 5 + "	" + c * 1.0 / 5);
//						FixHub.d4DetectionTimeList.clear();
//						FixHub.policyConjectureTimeList.clear();
//						FixHub.applyFixTimeList.clear();
//					}
//					System.err.println(FixHub.applyFixTimeList.size());
//				}
//				System.err.println("Formed data : " + FixHub.raceCount + "	" + FixHub.d4DetectionTime + "	" + FixHub.policyConjectureTime + "	" + FixHub.applyFixTime + "	" + FixHub.lockUsed);
//			}
//
//			@Override
//			public String getLabel() {
//				RepairPolicy repairPolicy = RepairPolicy.getInstance();
//				Queue<LockingPolicy> queue = repairPolicy.getLockingPolicies();
//				List<LockingPolicy> list = new ArrayList<>(queue);
//				Collections.sort(list);
//				LockingPolicy policy = list.get(list.size() - 1);
//				FixHub fixHub = FixHub.getInstance();
//				MemNode node = fixHub.markerNodeMap.get(marker.getId());
//				RaceMemUNode memUNode = RaceMemUNode.getNode(node);
//				for(Pair<RaceMemUNode, RaceLockUNode> pair : policy.getRepairList()){
//					if(memUNode.equals(pair.fst)){
//						RepairNode repairNode = pair.snd.getRepairNode();
//						if(repairNode.isNew()){
//							return "use new global static Object to protect race data";
//						}
//						else{
//							return String.format("use lock : {%s} to protect race data", repairNode.getLockName());
//						}
//					}
//				}
//				return "use locking policy to protect race data ";
//			}
//		};
//		resolutions[0] = resolution;
//		return resolutions;
//	}
//
//	@Override
//	public boolean hasResolutions(IMarker marker) {
//		FixHub fixHub = FixHub.getInstance();
//		MemNode node = null;
//		if (fixHub != null && fixHub.markerNodeMap.containsKey(marker.getId())) {
//			node = fixHub.markerNodeMap.get(marker.getId());
//		}
//		if (node != null) {
//			return true;
//		}
//		return false;
//	}
//}
