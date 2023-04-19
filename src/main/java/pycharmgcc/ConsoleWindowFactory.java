package pycharmgcc;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class ConsoleWindowFactory implements ToolWindowFactory {
    // this is setup in plugin.xml to create the custom console that shows GCC's outputs
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // get the console view that was created in the startup activity
        ConsoleView GCCConsole = project.getUserData(PyCharmGCCStartup.CONSOLE_VIEW_KEY);
        if (GCCConsole == null) {
            return;
        }
        // create the console's content
        ContentFactory contentFactory = ContentFactory.getInstance();
        toolWindow.getContentManager().addContent(contentFactory.createContent(GCCConsole.getComponent(), "", false)); // we don't need a displayName
    }
}