package ifix.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Stan Wang
 */
public class MyDialogWrapper extends DialogWrapper {

    String content;

    public MyDialogWrapper(String content) {
        // use current window as parent
        super(true);
        this.content = content;
        setTitle("IFix");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        JLabel label = new JLabel(content);
        label.setPreferredSize(new Dimension(100, 50));
        dialogPanel.add(label, BorderLayout.CENTER);
//        System.out.println("create center panel");
        return dialogPanel;
    }
}
