package fix.iDebugger.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import fix.iDebugger.handlers.FixHub;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod.SourcePosition;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.strings.Atom;

import edu.tamu.aser.tide.nodes.DLockNode;
import edu.tamu.aser.tide.nodes.MemNode;
import fix.iDebugger.util.DocumentUtil;
import fix.iDebugger.util.UpdateLineSet;
import fix.iDebugger.util.UseAST;

public class RepairPolicy{
	public final static int NUM_SOLUTION = 1;
	
	private static RepairPolicy repairPolicy = new RepairPolicy();
	
	public RaceConstraintRepository repository;
	public RaceConstraint constraint;
	
	private Queue<LockingPolicy> lockingPolicies;

	public static int lockIndex = 0;
	
	private int lowerBound;

	public CalSpace calSpace;
	
	public static RepairPolicy getInstance(){
		return repairPolicy;
	}
	
	private RepairPolicy(){
		repository = new RaceConstraintRepository();
		constraint = new RaceConstraint();
		lowerBound = 0;
		lockingPolicies = new PriorityQueue<>();
	}
	
	public void clear(){
		lockIndex = 0;
		lowerBound = 0;
		lockingPolicies.clear();
		RaceConstraint.clear();
		constraint = new RaceConstraint();
		repository.clear();
	}
	
	public void init(){
		for(String s : RaceMemUNode.sigToNode.keySet()){
			this.repository.addVariable(RaceMemUNode.sigToNode.get(s));
		}
		
		for(String s : RaceLockUNode.sigToNode.keySet()){
			this.repository.addLock(RaceLockUNode.sigToNode.get(s));
		}

		// add constraint of type A : lock(var1) = l1
		for(RaceMemUNode memUNode : this.repository.getVariableList()){
			
			Map<RaceLockUNode, Integer> allUsedLocks = memUNode.getAllUsedLocks();
			
			for(RaceLockUNode lockUNode : allUsedLocks.keySet()){
				constraint.addItemA(memUNode, lockUNode, allUsedLocks.get(lockUNode));
			}
		}
	}
	
	// show information of all share variable and lock
	public void showNodes(){
		System.out.println("----------------RaceMemUNode---------------");
		for(RaceMemUNode rNode : this.repository.getVariableList()){
			System.out.println(rNode.getSig());
			for(MemNode memNode : rNode.getAllNodes()){
				System.out.println(memNode);
			}
		}
		
		System.out.println("----------------RaceLockUNode--------------");
		for(RaceLockUNode lockUNode : this.repository.getLockList()){
			
			if(lockUNode.getRepairNode().isNew()){
				System.out.print(lockUNode.getSig() + " :");
				System.out.println(lockUNode.getRepairNode().getLockName());
			}
			else{
				System.out.println(lockUNode.getSig());
				for(DLockNode lockNode : lockUNode.getAllNodes()){
					System.out.println(lockNode);
				}
			}
			
		}
	}

	public String getMessage(){
		ArrayList<LockingPolicy> list = new ArrayList<>(lockingPolicies);
		Collections.sort(list);
		StringBuilder builder = new StringBuilder();
		for(int k = list.size() - 1; k >= 0; k --){
			for(Pair<RaceMemUNode, RaceLockUNode> pair : list.get(k).getRepairList()){
				builder.append(pair.fst).append(" using lock ").append(pair.snd).append(" | ");
			}
		}
		return builder.toString();
	}
	
	public void showPolicy(){
		System.out.println("============== show locking policy of " + lockingPolicies.size() + " :");
		ArrayList<LockingPolicy> list = new ArrayList<>(lockingPolicies);
		Collections.sort(list);
		for(int k = list.size() - 1; k >= 0; k --){
			System.out.println("============Locking policy " + k + " of score " + list.get(k).getScore() + "===========");
			
			Map<RaceMemUNode, RaceLockUNode> map = new HashMap<>();
			for(Pair<RaceMemUNode, RaceLockUNode> pair : list.get(k).getRepairList()){
				System.out.println(pair.fst + " using lock " + pair.snd);
				map.put(pair.fst, pair.snd);
			}

			List<RaceConstraintNode> satisfiedList = new ArrayList<>();
			List<RaceConstraintNode> unSatisfiedList = new ArrayList<>();
			for(RaceConstraintNode constraintNode : constraint.getList()){
				if(constraintNode.isSatisfied(map)){
					satisfiedList.add(constraintNode);
				}
				else{
					unSatisfiedList.add(constraintNode);
				}
			}
			System.out.println("Constraint status : ");
			System.out.println("Satisfied constraint: ");
			for(int i = 0; i < satisfiedList.size(); i ++){
				System.out.println(satisfiedList.get(i));
			}
			System.out.println("Unsatisfied constraint: ");
			for(int i = 0; i < unSatisfiedList.size(); i ++){
				System.out.println(unSatisfiedList.get(i));
			}
		}
	}
	
	public void generateLockingPolicy(){
		
//		System.out.println("start to generate locking policy");
		Map<RaceMemUNode, RaceLockUNode> res = new HashMap<>();
		lowerBound = 0;
		for(RaceMemUNode memUNode : repository.getVariableList()){
			RaceLockUNode maxUsedLock = null;
			int maxUsedCount = 0;
			for(RaceLockUNode lockUNode : memUNode.getAllUsedLocks().keySet()){
				if(maxUsedCount < memUNode.getAllUsedLocks().get(lockUNode)){
					maxUsedCount = memUNode.getAllUsedLocks().get(lockUNode);
					maxUsedLock = lockUNode;
				}
			}
			if(maxUsedCount > 0){
				res.put(memUNode, maxUsedLock);
				lowerBound += maxUsedCount;
//				System.out.println("variable " + memUNode + " initial with lock " + maxUsedLock + " with times " + maxUsedCount);
			}
		}
//		System.out.println("locking policy init finish");
//		System.out.println("start to apply bounded search algorithm");
		
		List<RaceMemUNode> varList = new ArrayList<>(repository.getVariableList());
		List<RaceLockUNode> lockList = new ArrayList<>(varList.size());
		this.calSpace = new CalSpace(repository.getLockList().size(), varList.size());
		calPolicyRecursively(varList, lockList, 0);
		// TODO different variable in different method should use different lock
//		Map<String, RepairNode> methodMap = new HashMap<>();
//		for(LockingPolicy policy : lockingPolicies){
//			for(Pair<RaceMemUNode, RaceLockUNode> pair : policy.getRepairList()){
//				if(pair.snd.getRepairNode().isNew()){
//					RepairNode forThis = null;
//					for(MemNode memNode : pair.fst.getAllNodes()){
//						String method = memNode.getBelonging().getMethod().toString();
//						if(methodMap.containsKey(method)){
//							forThis = methodMap.get(method);
//							break;
//						}
//					}
//					if(forThis != null){
//						pair.snd.setRepairNode(forThis);
//					}
//					else{
//						forThis = pair.snd.getRepairNode();
//						forThis.setLockName("sampleLock" + RepairNode.NEW_LOCK_INDEX ++);
//					}
//					
//					for(MemNode memNode : pair.fst.getAllNodes()){
//						String method = memNode.getBelonging().getMethod().toString();
//						if(! methodMap.containsKey(method)){
//							methodMap.put(method, forThis);
//						}
//					}
//				}
//			}
//		}
	}

	public void showBoundEffectiveness(){
		System.out.println("eff : " + this.calSpace.cur + " / " + this.calSpace.total + " : " + this.calSpace.getRes());
	}
	
	private void calPolicyRecursively(List<RaceMemUNode> varList, List<RaceLockUNode> lockList, int idx){
		Pair<Integer, Integer> scorePair = getMaxScore(varList, lockList, idx);
		int currentScore = scorePair.fst;
		int currentMaxScore = scorePair.snd;
		if((lockingPolicies.size() >= NUM_SOLUTION && currentMaxScore <= lowerBound)){
			this.calSpace.dec(idx);
			return;
		}

		if(idx == varList.size()){
			// new solution
			// there are no more than k solutions
			if(lockingPolicies.size() < NUM_SOLUTION){
				LockingPolicy lockingPolicy = new LockingPolicy();
				lockingPolicy.setScore(currentScore);
				for(int i = 0; i < varList.size(); i ++){
					lockingPolicy.addPair(Pair.make(varList.get(i), lockList.get(i)));
				}
				lockingPolicies.add(lockingPolicy);
				// set lower bound with the min score of top-k resolution
				if(lockingPolicies.size() == NUM_SOLUTION){
					lowerBound = lockingPolicies.peek().getScore();
				}
			}
			else{
				LockingPolicy firstPolicy = lockingPolicies.peek();
				if(currentScore > firstPolicy.getScore()){
					this.lowerBound = currentScore;
					LockingPolicy lockingPolicy = new LockingPolicy();
					lockingPolicy.setScore(currentScore);
					for(int i = 0; i < varList.size(); i ++){
						lockingPolicy.addPair(Pair.make(varList.get(i), lockList.get(i)));
					}
					lockingPolicies.poll();
					lockingPolicies.add(lockingPolicy);
				}
			}
			return;
		}
		for(RaceLockUNode lockUNode : repository.getLockList()){
			if(lockUNode.getRepairNode().canRepair(varList.get(idx))){
				lockList.add(lockUNode);
				calPolicyRecursively(varList, lockList, idx + 1);
				lockList.remove(idx);
			}
		}
	}
	
	private Pair<Integer, Integer> getMaxScore(List<RaceMemUNode> varList, List<RaceLockUNode> lockList, int idx) {
		int maxScore = 0;
		int currentScore = 0;
		RepairFeedBack feedBack = RepairFeedBack.getInstance();
		
		Map<RaceMemUNode, RaceLockUNode> map = new HashMap<>();
		for(int i = 0; i < idx; i ++){
			map.put(varList.get(i), lockList.get(i));
		}
		for(RaceConstraintNode constraintNode : constraint.getList()){
			if(constraintNode.getType() == RaceConstraintType.A){
				RaceMemUNode memUNode = (RaceMemUNode) constraintNode.getVar1();
			    RaceLockUNode lockUNode = (RaceLockUNode) constraintNode.getVar2();
			    if(map.get(memUNode) == null){
			    	maxScore += constraintNode.getScore();
			    }
			    else if (map.get(memUNode).equals(lockUNode)){
			    	currentScore += constraintNode.getScore();
			    	maxScore += constraintNode.getScore();
			    }
			    // maybe useless
			    int feedBackScore = feedBack.getScore(memUNode, lockUNode);
			    currentScore += feedBackScore;
			    maxScore += feedBackScore;
			}
			else if (constraintNode.getType() == RaceConstraintType.B){
				RaceMemUNode memUNode1 = (RaceMemUNode) constraintNode.getVar1();
				RaceMemUNode memUNode2 = (RaceMemUNode) constraintNode.getVar2();
				if(map.get(memUNode1) == null || map.get(memUNode2) == null){
					maxScore += constraintNode.getScore();
				}
				else if (map.get(memUNode1).equals(map.get(memUNode2))){
					currentScore += constraintNode.getScore();
					maxScore += constraintNode.getScore();
				}
			}
		}
		return Pair.make(currentScore, maxScore);
	}


	public int getRaceLine(MemNode raceNode) {
		IClass class1 = raceNode.getBelonging().getMethod().getDeclaringClass();
		Atom atom = class1.getName().getPackage();
		String className = class1.getName().getClassName().toString();
		String packageName = atom.toString().replaceAll("/", ".");
		int update = UpdateLineSet.hasUpdated(packageName, className);
		return raceNode.getLine() + update;
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
//		res.append("RepairPolicy [raceNode=" + raceNode + ", relatedNodes=");
//		for (RepairPolicy rPolicy : relatedNodes) {
//			res.append(rPolicy.getRaceNode() + ",");
//		}
//		res.append("allLocks=" + allLocks + ", suggestionLock=" + suggestionLock + "]");
		return res.toString();
	}


	
	public Queue<LockingPolicy> getLockingPolicies() {
		return lockingPolicies;
	}

	public void setLockingPolicies(Queue<LockingPolicy> lockingPolicies) {
		this.lockingPolicies = lockingPolicies;
	}
	
	public void chooseLockingPolicy(LockingPolicy lockingPolicy){
		this.lockingPolicies.clear();
		this.lockingPolicies.add(lockingPolicy);
		
		RepairFeedBack feedBack = RepairFeedBack.getInstance();
		feedBack.addNewPolicy(lockingPolicy);
		
	}
	
	public void applyLockingPolicy(){
		if(this.lockingPolicies.size() == 0){
			return;
		}
		ArrayList<LockingPolicy> tmpList = new ArrayList<>(repairPolicy.getLockingPolicies());
		Collections.sort(tmpList);
		LockingPolicy lockingPolicy = tmpList.get(tmpList.size() - 1);
		
		lockingPolicy.printLockInfo();
		
		FixHub fixHub = FixHub.getInstance();
		long start = System.currentTimeMillis();
		// get sorted list of all related nodes, lock all of them using
		// same lock
		
		Map<RaceLockUNode, HashSet<RaceMemUNode>> map = new HashMap<>();
		for(Pair<RaceMemUNode, RaceLockUNode> pair : lockingPolicy.getRepairList()){
			if(map.containsKey(pair.snd)){
				map.get(pair.snd).add(pair.fst);
			}
			else{
				HashSet<RaceMemUNode> set = new HashSet<>();
				set.add(pair.fst);
				map.put(pair.snd, set);
			}
		}
		
		// record each new static lock with file
		List<Pair<RepairNode, IFile>> newLockList = new ArrayList<>();
		
		for(RaceLockUNode lockUNode : map.keySet()){
			RepairNode suggestionLock = lockUNode.getRepairNode();
			HashSet<MemNode> nodesWithSameSuggessionLock = new HashSet<>();
			for(RaceMemUNode raceMemUNode : map.get(lockUNode)){
				nodesWithSameSuggessionLock.addAll(raceMemUNode.getAllNodes());
			}
			List<MemNode> allRelatedNodes = new ArrayList<>(nodesWithSameSuggessionLock);
			Collections.sort(allRelatedNodes, new MemNodeComparator());

			// include all nodes using same suggestion lock
			int lineStart = 0;
			int lineEnd = 0;
			int lineEndWithComment = 0;
			
			IDocument document = null;
			IFile lockFile = null;
			
			for (int i = 0; i < allRelatedNodes.size(); i++) {	
				MemNode beginNode = allRelatedNodes.get(i);

				if (DocumentUtil.policyIsLockedBySuggestionLock(beginNode, suggestionLock)) {
					continue;
				}
				
				lineStart = beginNode.getLine();
				lineEnd = lineStart;

				int lineWithLastUse = lineEnd;
				
				while(lineWithLastUse != 0){
					lineWithLastUse = UseAST.defInUseOut(lineStart, lineWithLastUse, beginNode.getFile().getLocation().toString());
					
					if(lineWithLastUse != lineEnd && lineWithLastUse != 0){
						lineEnd = lineWithLastUse;
					}
					else{
						lineWithLastUse = 0;
					}
				}
				
//				UseAST.useASTChangeLine(lineEnd,
//						allRelatedNodes.get(i).getFile().getLocation().toString());
				if (UseAST.crossBlock()) {
					lineEnd = UseAST.statementEnd;
				}
				
				
				
//				document = RepairNode.getDocument(beginNode.file);
				
				lineEndWithComment = DocumentUtil.ignoreComment(document, lineEnd);
				
				int j = i + 1;
				while (j < allRelatedNodes.size() &&
				// iff two nodes are in same class
						beginNode.getBelonging().getMethod().getDeclaringClass().toString()
						.equals(allRelatedNodes.get(j).getBelonging().getMethod().getDeclaringClass()
								.toString())) {
					// current node line, ignore comment
					int cLine = allRelatedNodes.get(j).getLine();
					if(cLine <= lineEndWithComment){
						j++;
						continue;
					}
					if (cLine == lineEndWithComment + 1) {
//						UseAST.useASTChangeLine(cLine,
//								allRelatedNodes.get(j).getFile().getLocation().toString());
						if (UseAST.crossBlock()) {
							lineEnd = UseAST.statementEnd;
						} else {
							lineEnd = cLine;
						}
						
					    lineWithLastUse = lineEnd;
						
						while(lineWithLastUse != 0){
							lineWithLastUse = UseAST.defInUseOut(lineStart, lineWithLastUse, beginNode.getFile().getLocation().toString());
							
							if(lineWithLastUse != lineEnd && lineWithLastUse != 0){
								lineEnd = lineWithLastUse;
							}
							else{
								lineWithLastUse = 0;
							}
						}
						
						lineEndWithComment = DocumentUtil.ignoreComment(document, lineEnd);
						j++;
					} else {
						break;
					}
				}
				
				if(suggestionLock.isNew()){
					suggestionLock.setInfo(beginNode);
					lockFile = beginNode.file;
				}

				try {
					int statementStart = 0;
					int statementEnd = 0;
					String currentText = document.get();

					IRegion statementInformation = null;

					statementInformation = document.getLineInformation(lineStart - 1);
					statementStart = statementInformation.getOffset();
					statementInformation = document.getLineInformation(lineEnd - 1);
					statementEnd = statementInformation.getOffset() + statementInformation.getLength();

					String statement = currentText.substring(statementStart, statementEnd);
					
					// judge if the text has comment at the end of line
					String[] statements = statement.split("\n");
					String lastSentence = statements[statements.length - 1];
					
					if(lastSentence.indexOf("//") != -1  || lastSentence.indexOf("/*") != -1){
						statementEnd = statementEnd - (lastSentence.length() - Math.max(lastSentence.indexOf("//"), lastSentence.indexOf("/*")));
						statement = currentText.substring(statementStart, statementEnd);
					}
					
					if (suggestionLock.isNew()) {
						if(suggestionLock.isInSameClass(beginNode)){
							document.replace(statementStart, statementEnd - statementStart,
									"synchronized(" + suggestionLock.getLockName() + ") { "
											+ statement.trim() + " }");
						}
						else{
							document.replace(statementStart, statementEnd - statementStart,
									"synchronized(" + suggestionLock.getPackageName() + "."
											+ suggestionLock.getClassName() + "." + suggestionLock.getLockName() + ") { "
											+ statement.trim() + " }");
						}
					} else {
						document.replace(statementStart, statementEnd - statementStart,
								"synchronized(" + suggestionLock.getLockName() + ") { " + statement.trim() + " }");
					}
					FixHub.lockUsed ++;
				} catch (BadLocationException e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				// remove unused lock
				// judge if a lock only used in one thread, which means
				// useless

				HashSet<DLockNode> removedLocks = new HashSet<>();
				HashSet<DLockNode> canNotRemovedLocks = new HashSet<>();
				
				for (int k = i; k < j; k++) {
					HashSet<DLockNode> cLocks = fixHub.nodeToLocks.get(allRelatedNodes.get(k));
					if (cLocks == null) {
						continue;
					}
					for (DLockNode dLockNode : cLocks) {
						if(lockUNode.getAllNodes().contains(dLockNode)){
							continue;
						}
						int endLine = dLockNode.getLine();
						if (!removedLocks.contains(dLockNode)
								&& !canNotRemovedLocks.contains(dLockNode)
								&& !dLockNode.equals(suggestionLock.getLockNode())) {
							
							// judge if the lock protect another share variable more than once
							boolean dFlag = false;
							
							HashSet<RaceMemUNode> memSet = map.get(RaceLockUNode.getNode(dLockNode));
							HashSet<MemNode> allNodes = new HashSet<>();
							if(memSet != null){
								for(RaceMemUNode node : memSet){
									allNodes.addAll(node.getAllNodes());
								}
								HashSet<MemNode> alreadyNodes = fixHub.lockToNodes.get(dLockNode);
								if(alreadyNodes != null && allNodes != null){
									alreadyNodes.retainAll(allNodes);
									if(allRelatedNodes.size() > 0){
										dFlag = true;
									}
								}
							}
							if(dFlag){
								canNotRemovedLocks.add(dLockNode);
								continue;
							}

							SSAInstruction[] instructions = dLockNode.getBelonging().getIR().getInstructions();
							for (int p = 0; p < instructions.length; p++) {
								if (instructions[p] != null) {
									if (dLockNode.inst.equals(instructions[p])) {
										try {// get source code line number
												// and ifile of this inst
											if (dLockNode.getBelonging().getIR()
													.getMethod() instanceof IBytecodeMethod) {
												int bytecodeindex = ((IBytecodeMethod) dLockNode.getBelonging()
														.getIR().getMethod())
																.getBytecodeIndex(instructions[p].iIndex());
												endLine = (int) dLockNode.getBelonging().getIR().getMethod()
														.getLineNumber(bytecodeindex);
											} else {
												SourcePosition position = dLockNode.getBelonging().getMethod()
														.getSourcePosition(instructions[p].iIndex());
												endLine = position.getLastLine();// .getLastLine();
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
										break;
									}
								}
							}

							try {
								int statementStart = 0;
								int statementend = 0;
								String currentText = document.get();

								IRegion statementInformation = null;
								statementInformation = document.getLineInformation(dLockNode.getLine() - 1);
								statementStart = statementInformation.getOffset();
								statementend = statementInformation.getOffset() + statementInformation.getLength();

								String statement = currentText.substring(statementStart, statementend);
								document.replace(statementStart, statementend - statementStart, "//" + statement);
								currentText = document.get();
								if(! statement.trim().endsWith("{")){
									
									statementInformation = document.getLineInformation(dLockNode.getLine());
									statementStart = statementInformation.getOffset();
									statementend = statementInformation.getOffset() + statementInformation.getLength();
									statement = currentText.substring(statementStart, statementend);
									if(statement.trim().startsWith("{")){
										statement = statement.replaceFirst("\\{", " ");
									}
									document.replace(statementStart, statementend - statementStart, statement);
								}
								// TODO remove prefix iff in the same class
								
								currentText = document.get();
								statementInformation = document.getLineInformation(endLine - 1);
								statementStart = statementInformation.getOffset();
								statementend = statementInformation.getOffset() + statementInformation.getLength();
								statement = currentText.substring(statementStart, statementend);
								document.replace(statementStart, statementend - statementStart, "//" + statement);
							} catch (BadLocationException e) {
								e.printStackTrace();
							}

							removedLocks.add(dLockNode);
						}
					}
				}

				i = j - 1;
			}

			// new static lock
			if (suggestionLock.isNew() && lockFile != null) {
				newLockList.add(Pair.make(suggestionLock, lockFile));
			}
		}
		
		// generate new static lock in each file
		for(Pair<RepairNode, IFile> pair : newLockList){
			RepairNode suggestionLock = pair.fst;
			IFile lockFile = pair.snd;
			IDocument document = null;
//			IDocument document = RepairNode.getDocument(lockFile);
			int line = suggestionLock.getLine();
			if (line != -1) {
				try {
					IRegion statementInformation = null;
					statementInformation = document.getLineInformation(line);
					int statementStart = statementInformation.getOffset();
					int statementend = statementInformation.getOffset() + statementInformation.getLength();
					String statement = document.get().substring(statementStart, statementend);
					document.replace(statementStart, statementend - statementStart,
							"    public static Object " + suggestionLock.getLockName()
									+ " = new Object();\n" + statement);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		
		fixHub.removeMarkers();
	}
	
	class MemNodeComparator implements Comparator<MemNode>{

		@Override
		public int compare(MemNode arg0, MemNode arg1) {
			if(arg0.getFile().toString().equals(arg1.getFile().toString())){
				return arg0.getLine() - arg1.getLine();
			}
			return arg0.getFile().toString().compareTo(arg1.getFile().toString());
		}
	}

	public class CalSpace{
		double total;
		double cur;
		int width;
		int depth;
		public CalSpace(int width, int depth){
			this.width = width;
			this.depth = depth;
			total = 1.0;
			for(int i = 1; i <= depth; i ++){
				total += Math.pow(width, i);
			}
			cur = total;
		}
		public void dec(int depth){
			int m = this.depth - depth;
			for (int i = 0; i <= m ; i++) {
				cur -= Math.pow(this.width, i);
			}
		}

		public double getRes(){
			return 1.0 - ((double)cur / (double)total);
		}
	}
}
