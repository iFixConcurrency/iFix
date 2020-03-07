package ifix.action;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiElement;
import fix.iDebugger.nodes.LockingPolicy;
import fix.iDebugger.nodes.RepairPolicy;
import ifix.FixManager;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ann
 */
public class FixNavigationHandler implements GutterIconNavigationHandler<PsiElement> {
    private String title = "fix suggestion";

    public class MyPopupStep extends BaseListPopupStep<String> {
        public MyPopupStep(List<String> menu) {
            super(title);
            this.init(title, menu, null);
        }
        @Nullable
        @Override
        public PopupStep onChosen(String selectedValue, boolean finalChoice) {
            if(MainAction.isFix){
                FixManager.getInstance().applyLockingPolicy();
            }
            else{

            }
            return super.onChosen(selectedValue, finalChoice);
        }
    }
    @Override
    public void navigate(MouseEvent e, PsiElement elt) {
        RepairPolicy policy = RepairPolicy.getInstance();
        ArrayList<LockingPolicy> list = new ArrayList<>(policy.getLockingPolicies());
        Collections.sort(list);
        ArrayList<String> res = new ArrayList<>();
        for(LockingPolicy p : list){
            res.add(p.getMessage());
        }
        if(res.size() <= 1){
            res.add("introduce new lock");
        }
        ListPopup listPopup = JBPopupFactory.getInstance().createListPopup(new MyPopupStep(res));
        listPopup.showInScreenCoordinates(e.getComponent(), e.getLocationOnScreen());
    }
}
