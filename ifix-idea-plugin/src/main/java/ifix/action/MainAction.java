package ifix.action;

import com.intellij.openapi.actionSystem.*;
import ifix.FixManager;
import ifix.view.RaceView;

import java.util.*;

public class MainAction extends AnAction {

    public static boolean isFix = true;

    public static Map<String, Set<Integer>> fileLineMap;

    private String className = "";

    private RaceView raceView;

    @Override
    public void actionPerformed(AnActionEvent e)  {
//        System.out.println(System.getProperty("java.class.path"));
        FixManager.getInstance().init(e);
        FixManager.getInstance().runIfix();
//        PsiManager.getInstanc
    }

}
