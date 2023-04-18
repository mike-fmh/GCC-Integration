package pycharmgcc;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GCCCompileCurrentFileAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        System.out.println("hello");
        System.out.println(event.getProject());
    }
}

