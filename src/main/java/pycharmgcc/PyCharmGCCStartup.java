package pycharmgcc;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

public class PyCharmGCCStartup implements StartupActivity {
    public static final Key<ConsoleView> CONSOLE_VIEW_KEY = new Key<>("CustomConsole.ConsoleView");
    // this public var will store the custom console view used to display gcc outputs
    @Override
    public void runActivity(@NotNull Project project) {
        // StartupActivity.runActivity -> Runs on IDE startup
        StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
            project.putUserData(CONSOLE_VIEW_KEY, new ConsoleViewImpl(project, true));
            // Store a new Console View instance in the project's userdata
            // so we don't need to recreate it everytime the keyboard shortcut is pressed
        });
    }
}
