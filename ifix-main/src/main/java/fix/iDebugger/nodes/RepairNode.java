package fix.iDebugger.nodes;

import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import edu.tamu.aser.tide.nodes.DLockNode;
import edu.tamu.aser.tide.nodes.INode;
import edu.tamu.aser.tide.nodes.MemNode;
import fix.iDebugger.util.UpdateLineSet;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.eclipse.ui.IEditorDescriptor;
//import org.eclipse.ui.IEditorPart;
//import org.eclipse.ui.IWorkbench;
//import org.eclipse.ui.IWorkbenchPage;
//import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.part.FileEditorInput;
//import org.eclipse.ui.part.ResourceTransfer;
//import org.eclipse.ui.texteditor.IDocumentProvider;
//import org.eclipse.ui.texteditor.ITextEditor;

public class RepairNode {
	@Override
	public String toString() {
		return "RepairNode [lockNode=" + lockNode + ", lockCount=" + lockCount + ", lockName=" + lockName + ", isNew="
				+ isNew + ", packageName=" + packageName + ", className=" + className + ", line=" + line + "]";
	}


	private DLockNode lockNode;
	// 锁使用的次数
	private int lockCount;
	
	private String lockName;
	private boolean isNew;
	private String packageName;
	private String className;
	private int line;
	
	public static int NEW_LOCK_INDEX = 0;

	public static RepairNode getNewStaticLock(String packageName, String className){
		RepairNode repairNode = new RepairNode("sampleLock" + NEW_LOCK_INDEX ++, true, packageName, className);
		return repairNode;
	}
	
	public void setInfo(MemNode memNode){
		IClass iClass = memNode.getBelonging().getMethod().getDeclaringClass();
		JavaClass javaClass = getMainClassNode(iClass);
		if(javaClass == null){
			System.err.println("failed to get belonging class when generate static lock");
		}
		else{
			this.packageName = javaClass.getName().getPackage().toString().replace('/', '.');
			this.className = javaClass.getName().getClassName().toString().replace('/', '.');
//			this.line = getPositionOfRaceNodeClassDefination(memNode);
		}
	}

	
//	public int getPositionOfRaceNodeClassDefination(MemNode raceNode) {
//		if (raceNode == null) {
//			System.err.println("Repair Policy get position error: try to get position of null");
//			return -1;
//		}
//		IClass tClass = raceNode.getBelonging().getMethod().getDeclaringClass();
//		JavaClass javaClass = getMainClassNode(tClass);
//		if (javaClass != null) {
//			IDocument document = getDocument(raceNode.getFile());
//			Position position = javaClass.getSourcePosition();
//			int line = position.getFirstLine() - 1;
//			line = DocumentUtil.ignoreComment(document, line);
//
//			String currentText = document.get();
//			try {
//				IRegion statementInformation = null;
//				String statement = null;
//
//				while (true) {
//					statementInformation = document.getLineInformation(line);
//					int statementStart = statementInformation.getOffset();
//					int statementend = statementInformation.getOffset() + statementInformation.getLength();
//					statement = currentText.substring(statementStart, statementend).trim();
//					if(statement.contains("{")){
//						break;
//					}
//					else{
//						line ++;
//					}
//
//				}
//
//			} catch (BadLocationException e) {
//				e.printStackTrace();
//			}
//
//			return line + 1;
//		} else {
//			System.err.println("Repair Policy get position error: try to get position of class of non JavaClass");
//			return -1;
//		}
//	}


	public boolean isInSameClass(MemNode ndoe){
		IClass iClass = ndoe.getBelonging().getMethod().getDeclaringClass();
		JavaClass javaClass = getMainClassNode(iClass);
		if(javaClass == null){
			System.err.println("failed to get belonging class when generate static lock");
			return false;
		}
		else{
			String packageName = javaClass.getName().getPackage().toString().replace('/', '.');
			String className = javaClass.getName().getClassName().toString().replace('/', '.');
			return this.className.equals(className) && this.packageName.equals(packageName);
		}
	}

	private static JavaClass getMainClassNode(IClass iClass) {
		if (iClass instanceof JavaClass) {
			JavaClass javaClass = (JavaClass) iClass;
			while (javaClass.getEnclosingClass() != null) {
				javaClass = (JavaClass) javaClass.getEnclosingClass();
			}
			String packageName = javaClass.getName().getPackage().toString();
			String className = javaClass.getName().getClassName().toString();
			if(packageName.contains("$") || className.contains("$")){
				Iterator<IClass> classes = javaClass.getClassLoader().iterateAllClasses();
				
				ArrayList<IClass> classList = new ArrayList<>();
				while(classes.hasNext()){
					classList.add(classes.next());
				}
				
				for(int i = 0; i < classList.size(); i ++){
					if(javaClass.equals(classList.get(i))){
						for(int j = i - 1; j >= 0; j --){
							iClass = classList.get(j);
							packageName = iClass.getName().getPackage().toString();
							className = iClass.getName().getClassName().toString();
							if(! packageName.contains("$") && ! className.contains("$")){
								break;
							}
						}
						break;
					}
				}
				javaClass = (JavaClass) iClass;
			}
			
			return javaClass;
		} else {
			System.err.println("cast error, can not cast iClass to javaclass when generate static loc");
			return (JavaClass) iClass;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + (isNew ? 1231 : 1237);
		result = prime * result + lockCount;
		result = prime * result + ((lockName == null) ? 0 : lockName.hashCode());
		result = prime * result + ((lockNode == null) ? 0 : lockNode.hashCode());
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RepairNode other = (RepairNode) obj;
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		if (isNew != other.isNew) {
			return false;
		}
		if (lockCount != other.lockCount) {
			return false;
		}
		if (lockName == null) {
			if (other.lockName != null) {
				return false;
			}
		} else if (!lockName.equals(other.lockName)) {
			return false;
		}
		if (lockNode == null) {
			if (other.lockNode != null) {
				return false;
			}
		} else if (!lockNode.equals(other.lockNode)) {
			return false;
		}
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		return true;
	}


	public RepairNode(String lockName, boolean isNew, String packageName, String className) {
		this.lockName = lockName;
		this.isNew = isNew;
		this.packageName = packageName;
		this.className = className;
	}
	
	
	public RepairNode(DLockNode lockNode, int lockCount){
		this.lockNode = lockNode;
		this.lockCount = lockCount;
		if(lockNode != null){
			this.line = lockNode.getLine();
			String info = lockNode.getBelonging().getMethod().getDeclaringClass().toString();
			info = info.substring(13, info.length());
			this.packageName = info.substring(0, info.lastIndexOf('/')).replace('/', '.');
			this.className = info.substring(info.lastIndexOf('/') + 1);
			this.lockName = generateLockName();
		}
	}
	
	
	private String generateLockName(){
		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(new File(lockNode.filePath)));
			while ((lockName = reader.readLine()) != null) {
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
		Matcher m = r.matcher(lockName);
		if(m.find()) {
			lockName = m.group(1);
			return lockName;
		}
		else {
			System.err.println("error found : " + lockName + " has not lock data");
			return "temp";
		}
	}
	
	public boolean canRepair(MemNode node){
		if(this.isNew){
			return true;
		}
		if(isPublicStaticField(this.lockNode, this.lockNode.inst.iIndex() - 1)){
			return true;
		}
		if(hasSameScope(node, this.lockNode)){
			return true;
		}
		return false;
	}
	
	public boolean canRepair(RaceMemUNode memUNode){
		for(MemNode memNode : memUNode.getAllNodes()){
			if(! canRepair(memNode)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 判断两个节点的类是否是同一个
	 * 
	 * @param node1
	 *            节点1
	 * @param node2
	 *            节点2
	 * @return boolean
	 */
	protected boolean hasSameScope(INode node1, INode node2) {
		IClass class1 = node1.getBelonging().getMethod().getDeclaringClass();
		IClass class2 = node2.getBelonging().getMethod().getDeclaringClass();
		return class1.toString().equals(class2.toString());
	}

	public boolean isPublicStaticField(INode node, int iindex) {
		if (iindex < 0) {
			return false;
		}
		SSAInstruction insts[] = node.getBelonging().getIR().getInstructions();
		IClass class1 = node.getBelonging().getMethod().getDeclaringClass();

		if (insts[iindex] instanceof SSAFieldAccessInstruction) {
			SSAFieldAccessInstruction si = (SSAFieldAccessInstruction) insts[iindex];
			IField field = class1.getField(si.getDeclaredField().getName());
			if(field == null){
				System.err.println("field of lock not found");
				return false;
			}
			return si.isStatic() && field.isPublic();
		}
		return false;

	}

	
	public int getLine() {
		return UpdateLineSet.hasUpdated(this.packageName, this.className) + line;
	}


	public void setLine(int line) {
		this.line = line;
	}


	public DLockNode getLockNode() {
		return lockNode;
	}


	public void setLockNode(DLockNode lockNode) {
		this.lockNode = lockNode;
	}


	public int getLockCount() {
		return lockCount;
	}


	public void setLockCount(int lockCount) {
		this.lockCount = lockCount;
	}


	public String getLockName() {
		return lockName;
	}


	public void setLockName(String lockName) {
		this.lockName = lockName;
	}


	public boolean isNew() {
		return isNew;
	}


	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}


	public String getPackageName() {
		return packageName;
	}


	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}


	public String getClassName() {
		return className;
	}


	public void setClassName(String className) {
		this.className = className;
	}

}
