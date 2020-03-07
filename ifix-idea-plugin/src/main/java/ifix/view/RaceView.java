package ifix.view;

import com.ibm.wala.util.collections.Pair;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import edu.tamu.aser.tide.engine.TIDEEngine;
import edu.tamu.aser.tide.engine.TIDERace;
import edu.tamu.aser.tide.nodes.MemNode;
import fix.iDebugger.handlers.FixHub;
import fix.iDebugger.nodes.LockingPolicy;
import fix.iDebugger.nodes.RaceLockUNode;
import fix.iDebugger.nodes.RaceMemUNode;
import fix.iDebugger.nodes.RepairPolicy;
import ifix.FixManager;
import ifix.action.MainAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Queue;
import java.util.*;

/**
 * @author ann
 */
public class RaceView implements ToolWindowFactory {

    private JPanel panel;
    private Tree tree;
    private JPopupMenu menu;
    private DefaultMutableTreeNode root;
    private Map<String, MemNode> treeNodeString2MemNode;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        FixManager.getInstance().setView(this);
        treeNodeString2MemNode = new HashMap<>();

        String title = "";
        if(FixHub.getInstance().engine != null){
            title = FixHub.getInstance().engine.races.size() + " races are detected, see more details at submenus. use Analyze->runIfix to trigger new Analysis";
        }
        else{
            title = "Race detection required, use Analyze->runIfix to trigger";
        }

        // create root node for tree
        root = new DefaultMutableTreeNode("IFix Race Information -- "  + title);

        // add tree to panel
        RepairPolicy policy = RepairPolicy.getInstance();
        for(RaceMemUNode node : policy.repository.getVariableList()){
            for(MemNode memNode : node.getAllNodes()){
//                System.out.println(memNode.filePath);
//                System.out.println(memNode.getLine());
            }
        }
        



        tree = new Tree(root);

        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(tree);
//        panel.add(b);
//        panel.add(c);
        // double-click listener
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 如果在这棵树上点击了2次,即双击
                if (e.getSource() == tree && e.getClickCount() == 2) {
                    // 按照鼠标点击的坐标点获取路径
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    if (selPath != null)// 谨防空指针异常!双击空白处是会这样
                    {
//                        System.out.println(selPath);// 输出路径看一下
                        // 获取这个路径上的最后一个组件,也就是双击的地方
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                        // 输出这个组件toString()的字符串看一下
//                        System.out.println("choose " + node.toString());
                        if (node.isLeaf()) {
                            String fileName = node.toString().substring(node.toString().indexOf("filePath='"));
                            fileName = fileName.substring(10);
                            fileName = fileName.substring(0, fileName.indexOf("'"));
                            String lineNum = node.toString().substring(node.toString().indexOf("bugLine='"));
                            lineNum = lineNum.substring(9);
                            lineNum = lineNum.substring(0, lineNum.indexOf("'"));
                            FixManager.getInstance().showPsiFile(fileName, Integer.parseInt(lineNum), 0);
                        }
                    }
                } else if (e.getSource() == tree && e.getButton() == MouseEvent.BUTTON3) {
                    if(MainAction.isFix){
                        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                        if (selPath != null) {
                            System.out.println(selPath);
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                            if (node.isLeaf()) {
//                                System.out.println("choose " + node.toString());
                                MemNode memNode = treeNodeString2MemNode.get(node.toString());
                                menu = new JPopupMenu();
                                Queue<LockingPolicy> lockingPolicies = RepairPolicy.getInstance().getLockingPolicies();
                                for (LockingPolicy lp : lockingPolicies) {
                                    for (Pair<RaceMemUNode, RaceLockUNode> p : lp.getRepairList()) {
                                        if (p.fst.equals(RaceMemUNode.getNode(memNode))) {
                                            //将 var:lock 信息添加到右键菜单列表中
                                            JMenuItem item = new JMenuItem("Using Lock [ " + p.snd.getSig() + "] to protect variable [ " + p.fst.getSig() + "].");
                                            menu.add(item);
                                            item.addActionListener(e1 -> {
                                                RepairPolicy.getInstance().chooseLockingPolicy(lp);
                                                FixManager.getInstance().applyLockingPolicy();
                                            });
                                        }
                                    }
                                }
                                menu.show(panel, e.getX(), e.getY());
                            }
                        }
                    }
                }
            }
        });

        // create content
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);

        // show content
        toolWindow.getContentManager().addContent(content);
    }

    // validate the tree according to race information
    public void showRace() {
        List<RaceInfo> raceInfoList = new ArrayList<>();
        int i = 0;
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        root.removeAllChildren();
        model.reload();
        for (TIDERace node : Objects.requireNonNull(TIDEEngine.getEngine()).races) {
            RaceItem raceItem1 = new RaceView.RaceItem(node.node1.prefix, node.node1.filePath, getClassName(node.node1.className), node.node1.getLine());
            RaceItem raceItem2 = new RaceView.RaceItem(node.node2.prefix, node.node2.filePath, getClassName(node.node2.className), node.node2.getLine());
            raceInfoList.add(new RaceView.RaceInfo(raceItem1, raceItem2));
            treeNodeString2MemNode.put(raceItem1.toString(), node.node1);
            treeNodeString2MemNode.put(raceItem2.toString(), node.node2);
            String msg = raceItem1.getClassName() + ":" + raceItem1.getBugLine() + "<->" +
                    raceItem2.getClassName() + ":" + raceItem2.getBugLine();
            DefaultMutableTreeNode dnode = new DefaultMutableTreeNode(" RaceInfo" + (i++) + "-> " + msg);
            dnode.add(new DefaultMutableTreeNode(raceInfoList.get(i - 1).item1));
            dnode.add(new DefaultMutableTreeNode(raceInfoList.get(i - 1).item2));

            root.add(dnode);
        }
        tree.revalidate();
        tree.expandPath(new TreePath(root.getPath()));
    }

    private String getClassName(String classname) {
        if (classname.contains("java/util/")) {
            return classname.substring(classname.indexOf("L") + 1, classname.length() - 1).replace("$", "");
        } else {
            return classname.substring(classname.indexOf(':') + 3, classname.length()).replace("$", "");
        }
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return true;
    }

    public static class RaceInfo {
        RaceItem item1;
        RaceItem item2;

        public RaceInfo(RaceItem item1, RaceItem item2) {
            this.item1 = item1;
            this.item2 = item2;
        }
    }

    public static class TraceItem {
        private String line;
        private String fileName;
        private String msg;

        public TraceItem(String line, String fileName, String msg) {
            this.line = line;
            this.fileName = fileName;
            this.msg = msg;
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    public static class RaceItem {
        private String infoMessage;

        private String varName;

        private String fileName;

        private String className;

        private int bugLine;

        public List<TraceItem> getTrace() {
            return trace;
        }

        public void setTrace(List<TraceItem> trace) {
            this.trace = trace;
        }

        private List<TraceItem> trace;

        public RaceItem(String varName, String fileName, String className, int bugLine) {
            this.varName = varName;
            this.fileName = fileName;
            this.className = className;
            this.bugLine = bugLine;
        }

        public String getInfoMessage() {
            return this.toString();
        }

        public void setInfoMessage(String infoMessage) {
            this.infoMessage = infoMessage;
        }

        public String getVarName() {
            return varName;
        }

        public void setVarName(String varName) {
            this.varName = varName;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public int getBugLine() {
            return bugLine;
        }

        public void setBugLine(int bugLine) {
            this.bugLine = bugLine;
        }

        @Override
        public String toString() {
            String tmpFileName = this.fileName;
            String msg = "";
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            if(projects.length > 0){
                for(Project project : projects){
                    if(tmpFileName.contains(project.getBasePath())){
                        tmpFileName = tmpFileName.replace(project.getBasePath(), "");
                    }
                }
            }
            while(tmpFileName.startsWith("/")){
                tmpFileName = tmpFileName.substring(1);
            }

            return "RaceItem{" +
                    "varName='" + varName + '\'' +
                    ", filePath='" + tmpFileName + '\'' +
                    ", className='" + className + '\'' +
                    ", bugLine='" + bugLine + '\'' +
                    '}';
        }
    }

}




