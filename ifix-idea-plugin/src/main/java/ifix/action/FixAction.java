package ifix.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ann
 */
public class FixAction extends AnAction {

    private static Logger logger = (Logger) LoggerFactory.getLogger(FixAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Editor is known to exist from update, so it's not null
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        // Get the action manager in order to get the necessary action handler...
        final EditorActionManager actionManager = EditorActionManager.getInstance();
        // Get the action handler registered to clone carets
        final EditorActionHandler actionHandler = actionManager.getActionHandler(IdeActions.ACTION_EDITOR_CLONE_CARET_BELOW);
        // Clone one caret below the active caret
        actionHandler.execute(editor, editor.getCaretModel().getPrimaryCaret(), e.getDataContext());
    }

    /**
     * Enables and sets visibility of this action menu item if:
     *   A project is open,
     *   An editor is active,
     *   At least one caret exists
     * @param e  Event related to this action
     */
    @Override
    public void update(@NotNull final AnActionEvent e) {
        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        // Make sure at least one caret is available
        boolean menuAllowed = false;
        boolean needFix = false;
        if (editor != null && project != null) {
            // Ensure the list of carets in the editor is not empty
            menuAllowed = !editor.getCaretModel().getAllCarets().isEmpty();

            Caret currentCaret = editor.getCaretModel().getCurrentCaret();
            if(currentCaret != null){
                int line = currentCaret.getLogicalPosition().line;
//                System.out.println();

                if(MainAction.fileLineMap.get("wronglock.Main").contains(line)){
                    needFix = true;
                }
            }
        }
        e.getPresentation().setEnabledAndVisible(menuAllowed && needFix);
    }
}
