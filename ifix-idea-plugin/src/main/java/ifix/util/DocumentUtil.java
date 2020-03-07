package ifix.util;

import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl;
import com.ibm.wala.cast.tree.CAstSourcePositionMap;
import com.ibm.wala.classLoader.IClass;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import edu.tamu.aser.tide.nodes.DLockNode;
import edu.tamu.aser.tide.nodes.MemNode;
import fix.iDebugger.handlers.FixHub;
import fix.iDebugger.nodes.RaceLockUNode;
import fix.iDebugger.nodes.RepairNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author ann
 */
public class DocumentUtil {

	public static boolean policyIsLockedBySuggestionLock(MemNode raceNode, RepairNode repairNode) {
		boolean hasLocked = false;
		FixHub fixHub = FixHub.getInstance();
		HashSet<DLockNode> currentLocks = fixHub.nodeToLocks.get(raceNode);
		if (currentLocks != null) {
			for (DLockNode lockNode : currentLocks) {
				if(repairNode.equals(RaceLockUNode.getNode(lockNode).getRepairNode())){
					hasLocked = true;
					break;
				}
			}
		}
		return hasLocked;
	}

	public static int ignoreComment(Document document, int line, boolean needTheFirstLine) {
		if(!needTheFirstLine){
			line += 1;
		}
		String currentText = document.getText();
		String statement = null;
		while (true) {
			int statementStart = document.getLineStartOffset(line);
			int statementend = document.getLineEndOffset(line);
			statement = document.getText(new TextRange(statementStart, statementend)).trim();
			if (statement.startsWith("/*")) {
				if (statement.endsWith("*/")) {
					line++;
					continue;
				}
				do {
					line++;
					statementStart = document.getLineStartOffset(line);
					statementend = document.getLineEndOffset(line);
					statement = document.getText(new TextRange(statementStart, statementend)).trim();
				} while (!statement.endsWith("*/"));
				line++;
			} else if (statement.startsWith("//") || statement.length() == 0) {
				line++;
				continue;
			}
			else{
				break;
			}
		}
		if(!needTheFirstLine){
			line--;
		}
		return line;
	}

	public static int ignoreComment(Document document, int cLine) {
		return  ignoreComment(document, cLine, false);
	}

	public static void replaceString(Document document, int startOffset, int endOffset, String replacement){
		Project[] projects =  ProjectManager.getInstance().getOpenProjects();
		if(projects.length == 0){
			System.err.println("project not found !!!");
			return;
		}
		Project project = projects[0];

		WriteCommandAction.runWriteCommandAction(project, () -> {
			document.replaceString(startOffset, endOffset, replacement);
		});
	}

	public static void setInfo(RepairNode repairNode, MemNode memNode){
		IClass iClass = memNode.getBelonging().getMethod().getDeclaringClass();
		JavaSourceLoaderImpl.JavaClass javaClass = getMainClassNode(iClass);
		if(javaClass == null){
			System.err.println("failed to get belonging class when generate static lock");
		}
		else{
			repairNode.setPackageName(javaClass.getName().getPackage().toString().replace('/', '.'));
			repairNode.setClassName(javaClass.getName().getClassName().toString().replace('/', '.'));
			repairNode.setLine(getPositionOfRaceNodeClassDefination(memNode));
		}
	}

	private static int getPositionOfRaceNodeClassDefination(MemNode raceNode){

		if (raceNode == null) {
			System.err.println("Repair Policy get position error: try to get position of null");
			return -1;
		}
		IClass tClass = raceNode.getBelonging().getMethod().getDeclaringClass();
		JavaSourceLoaderImpl.JavaClass javaClass = getMainClassNode(tClass);
		if (javaClass != null) {
			VirtualFile currFile = LocalFileSystem.getInstance().findFileByIoFile(new File(raceNode.filePath));
			Document document = FileDocumentManager.getInstance().getDocument(currFile);

			CAstSourcePositionMap.Position position = javaClass.getSourcePosition();
			int line = position.getFirstLine() - 1;
			line = DocumentUtil.ignoreComment(document, line, true);
			String statement = null;

			while (true) {

				int statementStart = document.getLineStartOffset(line);
				int statementEnd = document.getLineEndOffset(line);
				statement = document.getText(new TextRange(statementStart, statementEnd));
				if(statement.contains("{")){
					break;
				}
				else{
					line ++;
				}
			}

			return line + 1;
		} else {
			System.err.println("Repair Policy get position error: try to get position of class of non JavaClass");
			return -1;
		}
	}

	private static JavaSourceLoaderImpl.JavaClass getMainClassNode(IClass iClass) {
		if (iClass instanceof JavaSourceLoaderImpl.JavaClass) {
			JavaSourceLoaderImpl.JavaClass javaClass = (JavaSourceLoaderImpl.JavaClass) iClass;
			while (javaClass.getEnclosingClass() != null) {
				javaClass = (JavaSourceLoaderImpl.JavaClass) javaClass.getEnclosingClass();
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
				javaClass = (JavaSourceLoaderImpl.JavaClass) iClass;
			}

			return javaClass;
		} else {
			System.err.println("cast error, can not cast iClass to javaclass when generate static loc");
			return (JavaSourceLoaderImpl.JavaClass) iClass;
		}
	}
}
