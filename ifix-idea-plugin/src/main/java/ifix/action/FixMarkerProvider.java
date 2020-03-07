package ifix.action;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import ifix.view.MarkerManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ann
 */
public class FixMarkerProvider implements LineMarkerProvider {

    private static Logger logger = (Logger) LoggerFactory.getLogger(FixMarkerProvider.class);

    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
//        logger.info(element + " passed in");
        if(MarkerManager.getInstance().shouldMark(element)){
            LineMarkerInfo info = new LineMarkerInfo<>(element, element.getTextRange(), AllIcons.General.Error,
                    e -> "click to show fix suggestion",
                    new FixNavigationHandler(),
                    GutterIconRenderer.Alignment.CENTER);
            return info;
        }
        return null;
    }


}
