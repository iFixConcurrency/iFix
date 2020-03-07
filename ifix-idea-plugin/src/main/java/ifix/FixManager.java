package ifix;

import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.collections.Pair;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import edu.tamu.aser.tide.engine.TIDEEngine;
import edu.tamu.aser.tide.nodes.DLockNode;
import edu.tamu.aser.tide.nodes.MemNode;
import fix.iDebugger.handlers.DetectionHub;
import fix.iDebugger.handlers.FixHub;
import fix.iDebugger.nodes.*;
import ifix.util.DocumentUtil;
import ifix.util.UseAST;
import ifix.view.MarkerManager;
import ifix.view.MyDialogWrapper;
import ifix.view.RaceView;

import java.io.File;
import java.util.*;

/**
 * @author ann
 */
public class FixManager {


    private String className;
    private Map<String, List<String>> cpMap;
    private AnActionEvent e;
    private RaceView view;

    private FixManager() {

    }

    private static FixManager manager = new FixManager();

    public static FixManager getInstance() {
        return manager;
    }

    public void runIfix() {
        try {
            TIDEEngine.setIsPlugin(true);
            System.out.println("start to process " + className);
            DetectionHub hub = new DetectionHub(cpMap, className);
            hub.detect();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        FixHub fixHub = FixHub.getInstance();
        fixHub.clear();

        if (fixHub.handleFix()) {
            MarkerManager.getInstance().generateMarker();

            int raceCount = fixHub.engine.races.size();
//            Editor editor = e.getData(PlatformDataKeys.EDITOR);
//            Color color = editor.getColorsScheme().getColor(EditorColors.CARET_COLOR);
//            editor.getMarkupModel().addLineHighlighter(1 , HighlighterLayer.ADDITIONAL_SYNTAX, new TextAttributes(JBColor.RED, null, color, EffectType.ROUNDED_BOX, Font.PLAIN ));
//            System.out.println("highlight success");

//            fixHub.addMarker(project);
//            fixHub.initialGUI();
//            FixHub.policyConjectureTime = System.currentTimeMillis() - startFix;
            new MyDialogWrapper("Finish! " +  raceCount + " races detected, see race view below for more details").showAndGet();

            System.err.println("policy conjecture cost : " + FixHub.policyConjectureTime);
            RepairPolicy policy = RepairPolicy.getInstance();
            if(view != null){
                view.showRace();
            }
        } else {
            new MyDialogWrapper("Failed to handle fix.").showAndGet();
        }
    }

    public void applyLockingPolicy() {
        long startTime = System.currentTimeMillis();
        RepairPolicy policy = RepairPolicy.getInstance();
        if (policy.getLockingPolicies().size() == 0) {
            System.err.println("no locking policy found");
            return;
        }
        ArrayList<LockingPolicy> tmpList = new ArrayList<>(policy.getLockingPolicies());
        Collections.sort(tmpList);
        LockingPolicy lockingPolicy = tmpList.get(tmpList.size() - 1);

        lockingPolicy.printLockInfo();

        FixHub fixHub = FixHub.getInstance();
        long start = System.currentTimeMillis();
        // get sorted list of all related nodes, lock all of them using
        // same lock

        Map<RaceLockUNode, HashSet<RaceMemUNode>> map = new HashMap<>();
        for (Pair<RaceMemUNode, RaceLockUNode> pair : lockingPolicy.getRepairList()) {
            if (map.containsKey(pair.snd)) {
                map.get(pair.snd).add(pair.fst);
            } else {
                HashSet<RaceMemUNode> set = new HashSet<>();
                set.add(pair.fst);
                map.put(pair.snd, set);
            }
        }

        // record each new static lock with file
        List<Pair<RepairNode, VirtualFile>> newLockList = new ArrayList<>();

        for (RaceLockUNode lockUNode : map.keySet()) {
            RepairNode suggestionLock = lockUNode.getRepairNode();
            HashSet<MemNode> nodesWithSameSuggestionLock = new HashSet<>();
            for (RaceMemUNode raceMemUNode : map.get(lockUNode)) {
                nodesWithSameSuggestionLock.addAll(raceMemUNode.getAllNodes());
            }
            List<MemNode> allRelatedNodes = new ArrayList<>(nodesWithSameSuggestionLock);
            Collections.sort(allRelatedNodes, new MemNodeComparator());

            // include all nodes using same suggestion lock
            int lineStart = 0;
            int lineEnd = 0;
            int lineEndWithComment = 0;

            VirtualFile lockFile = null;

            for (int i = 0; i < allRelatedNodes.size(); i++) {
                MemNode beginNode = allRelatedNodes.get(i);

                if (DocumentUtil.policyIsLockedBySuggestionLock(beginNode, suggestionLock)) {
                    continue;
                }

                lineStart = beginNode.getLine();
                lineEnd = lineStart;

                int lineWithLastUse = lineEnd;

                while (lineWithLastUse != 0) {
                    lineWithLastUse = UseAST.defInUseOut(lineStart, lineWithLastUse, beginNode.filePath);

                    if (lineWithLastUse != lineEnd && lineWithLastUse != 0) {
                        lineEnd = lineWithLastUse;
                    } else {
                        lineWithLastUse = 0;
                    }
                }

//                UseAST.useASTChangeLine(lineEnd,
//                        allRelatedNodes.get(i).filePath);
                if (UseAST.crossBlock()) {
                    lineEnd = UseAST.statementEnd;
                }

//				document = RepairNode.getDocument(beginNode.file);

                VirtualFile currFile = LocalFileSystem.getInstance().findFileByIoFile(new File(beginNode.filePath));

                Document document = FileDocumentManager.getInstance().getDocument(currFile);

                lineEndWithComment = DocumentUtil.ignoreComment(document, lineEnd);
                int j = i + 1;
                while (j < allRelatedNodes.size() &&
                        // iff two nodes are in same class
                        beginNode.getBelonging().getMethod().getDeclaringClass().toString()
                                .equals(allRelatedNodes.get(j).getBelonging().getMethod().getDeclaringClass()
                                        .toString())) {
                    // current node line, ignore comment
                    int cLine = allRelatedNodes.get(j).getLine();
                    if (cLine <= lineEndWithComment) {
                        j++;
                        continue;
                    }
                    if (cLine == lineEndWithComment + 1) {
//                        UseAST.useASTChangeLine(cLine,
//                                allRelatedNodes.get(j).filePath);
                        if (UseAST.crossBlock()) {
                            lineEnd = UseAST.statementEnd;
                        } else {
                            lineEnd = cLine;
                        }

                        lineWithLastUse = lineEnd;

                        while (lineWithLastUse != 0) {
                            lineWithLastUse = UseAST.defInUseOut(lineStart, lineWithLastUse, beginNode.filePath);

                            if (lineWithLastUse != lineEnd && lineWithLastUse != 0) {
                                lineEnd = lineWithLastUse;
                            } else {
                                lineWithLastUse = 0;
                            }
                        }

                        lineEndWithComment = DocumentUtil.ignoreComment(document, lineEnd);
                        j++;
                    } else {
                        break;
                    }
                }


                if (suggestionLock.isNew() && (suggestionLock.getPackageName() == null || "".equals(suggestionLock.getPackageName()))) {
                    DocumentUtil.setInfo(suggestionLock, beginNode);
                    lockFile = LocalFileSystem.getInstance().findFileByIoFile(new File(beginNode.filePath));
                }

                int statementStart = 0;
                int statementEnd = 0;
                String currentText = document.getText();

                statementStart = document.getLineStartOffset(lineStart - 1);
                statementEnd = document.getLineEndOffset(lineEnd - 1);

                String statement = document.getText(new TextRange(statementStart, statementEnd));

                // judge if the text has comment at the end of line
                String[] statements = statement.split("\n");
                String lastSentence = statements[statements.length - 1];

                if (lastSentence.indexOf("//") != -1 || lastSentence.indexOf("/*") != -1) {
                    statementEnd = statementEnd - (lastSentence.length() - Math.max(lastSentence.indexOf("//"), lastSentence.indexOf("/*")));
                    statement = currentText.substring(statementStart, statementEnd);
                }

                if (suggestionLock.isNew()) {
                    if (suggestionLock.isInSameClass(beginNode)) {
                        DocumentUtil.replaceString(document, statementStart, statementEnd,
                                "synchronized(" + suggestionLock.getLockName() + ") { "
                                        + statement.trim() + " }");
                    } else {
                        DocumentUtil.replaceString(document, statementStart, statementEnd,
                                "synchronized(" + suggestionLock.getPackageName() + "."
                                        + suggestionLock.getClassName() + "." + suggestionLock.getLockName() + ") { "
                                        + statement.trim() + " }");
                    }
                } else {
                    DocumentUtil.replaceString(document, statementStart, statementEnd,
                            "synchronized(" + suggestionLock.getLockName() + ") { " + statement.trim() + " }");
                }
                FixHub.lockUsed++;

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
                        if (lockUNode.getAllNodes().contains(dLockNode)) {
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
                            if (memSet != null) {
                                for (RaceMemUNode node : memSet) {
                                    allNodes.addAll(node.getAllNodes());
                                }
                                HashSet<MemNode> alreadyNodes = fixHub.lockToNodes.get(dLockNode);
                                if (alreadyNodes != null && allNodes != null) {
                                    alreadyNodes.retainAll(allNodes);
                                    if (allRelatedNodes.size() > 0) {
                                        dFlag = true;
                                    }
                                }
                            }
                            if (dFlag) {
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
                                                IMethod.SourcePosition position = dLockNode.getBelonging().getMethod()
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

                            statementStart = 0;
                            statementEnd = 0;
                            currentText = document.getText();

                            statementStart = document.getLineStartOffset(dLockNode.getLine() - 1);
                            statementEnd = document.getLineEndOffset(dLockNode.getLine() - 1);

                            statement = currentText.substring(statementStart, statementEnd);
                            DocumentUtil.replaceString(document, statementStart, statementEnd, "//" + statement);
                            currentText = document.getText();
                            if (!statement.trim().endsWith("{")) {
                                statementStart = document.getLineStartOffset(dLockNode.getLine());
                                statementEnd = document.getLineEndOffset(dLockNode.getLine());
                                statement = currentText.substring(statementStart, statementEnd);
                                if (statement.trim().startsWith("{")) {
                                    statement = statement.replaceFirst("\\{", " ");
                                }
                                DocumentUtil.replaceString(document, statementStart, statementEnd, statement);
                            }
                            // TODO remove prefix iff in the same class

                            currentText = document.getText();
                            statementStart = document.getLineStartOffset(endLine - 1);
                            statementEnd = document.getLineEndOffset(endLine - 1);
                            statement = currentText.substring(statementStart, statementEnd);
                            DocumentUtil.replaceString(document, statementStart, statementEnd, "//" + statement);

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
        for (Pair<RepairNode, VirtualFile> pair : newLockList) {
            RepairNode suggestionLock = pair.fst;
            VirtualFile lockFile = pair.snd;
            Document document = null;
            document = FileDocumentManager.getInstance().getDocument(lockFile);
            int line = suggestionLock.getLine();
            if (line != -1) {
                int statementStart = document.getLineStartOffset(line);
                int statementend = document.getLineEndOffset(line);
                String statement = document.getText(new TextRange(statementStart, statementend));
                DocumentUtil.replaceString(document, statementStart, statementend,
                        "    // added by iFix\n    public static Object " + suggestionLock.getLockName()
                                + " = new Object();\n" + statement);
            }
        }
        MarkerManager.getInstance().removeMarkers();
        System.out.println("Fix cost : " + (System.currentTimeMillis() - startTime));
//        runIfix();
    }

    public void setView(RaceView view) {
        this.view = view;
    }

    class MemNodeComparator implements Comparator<MemNode> {

        @Override
        public int compare(MemNode arg0, MemNode arg1) {
            if (arg0.filePath.equals(arg1.filePath)) {
                return arg0.getLine() - arg1.getLine();
            }
            return arg0.filePath.compareTo(arg1.filePath);
        }
    }

    private Map<String, List<String>> obtainClassPath(AnActionEvent e) {
        // get current project for action
        Project project = e.getData(PlatformDataKeys.PROJECT);

        ModuleManager mm = ModuleManager.getInstance(project);
        Module[] modules = mm.getModules();
        Map<String, List<String>> map = new HashMap<>(modules.length);
        for (Module m : modules) {
            map.put("SOURCE", new ArrayList<>());
            map.put("Primordial", new ArrayList<>());
            map.put("Application", new ArrayList<>());
            map.put("Extension", new ArrayList<>());
            // source dir
            VirtualFile[] files = ModuleRootManager.getInstance(m).getSourceRoots();
            for (VirtualFile vf : files) {
                map.get("SOURCE").add(vf.getPath());
            }
            // dependencies, sdk only
            files = OrderEnumerator.orderEntries(m).sdkOnly().recursively().getClassesRoots();
            for (VirtualFile vf : files) {
                map.get("Primordial").add(vf.getPath().replace("!/", ""));
            }
            // ext library
            files = OrderEnumerator.orderEntries(m).librariesOnly().recursively().getClassesRoots();
            for (VirtualFile vf : files) {
                map.get("Extension").add(vf.getPath().replace("!/", ""));
            }
        }
        return map;
    }

    public void init(AnActionEvent e) {
        // get file for action
        this.e = e;
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        String pkgName = "";
        className = "";
        // obtain package name
        ASTNode[] children = psiFile.getNode().getChildren(null);
        for (ASTNode child : children) {
            if (child.getElementType().toString().equals("PACKAGE_STATEMENT")) {
                pkgName = child.getText().replace("package", "").replace(";", "").trim();
            }
            if (child.getElementType().toString().equals("CLASS")) {
                boolean flag = false;
                for (ASTNode subNode : child.getChildren(null)) {
                    if(subNode.getElementType().toString().equals("MODIFIER_LIST")){
                        for(ASTNode modifier : subNode.getChildren(null)){
                            if(modifier.getText().equals("public")){
                                flag = true;
                            }
                        }
                    }
                    if(flag) {
                        if (subNode.getElementType().toString().equals("IDENTIFIER")) {
                            className = subNode.getText();
                            break;
                        }
                    }
                }

            }
        }
        if (!"".equals(pkgName)) {
            className = pkgName + "/" + className;
        }

        cpMap = obtainClassPath(e);
    }

    Project getCurrentOpenedProject(){
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if(projects.length > 0){
            return projects[0];
        }
        return null;
    }

    public void showPsiFile(String name, int lineNumber, int column) {
        VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(name);
        if(vf == null){
            Project project = getCurrentOpenedProject();
            vf = LocalFileSystem.getInstance().findFileByPath(project.getBasePath() + "/" + name);
        }
        OpenFileDescriptor desc = new OpenFileDescriptor(e.getProject(), vf, lineNumber - 1, column);
        desc.navigate(true);

//        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
//        int caretLocation = editor.getCaretModel().getOffset();
//        int startOffset = caretLocation;
//        int endOffset = startOffset + length;
//
//        for (RangeHighlighter highlighter : editor.getMarkupModel().getAllHighlighters()) {
//            highlighter.dispose();
//        }
//
//        editor.getMarkupModel().addRangeHighlighter(startOffset, endOffset, HighlighterLayer.ERROR, codeHighlightTextAttributes, HighlighterTargetArea.EXACT_RANGE);
    }
}
