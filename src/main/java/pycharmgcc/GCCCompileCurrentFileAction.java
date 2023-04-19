package pycharmgcc;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class GCCCompileCurrentFileAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject(); // user's project instance
        if (project != null) {
            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor(); // editor session
            if (editor == null) {
                return;
            }

            // GET CURRENT FILE's PATH
            // we need to get the current file as a
            // Document -> PsiFile -> String
            // to get the absolute filepath
            Document currentDoc = null;
            PsiFile psiFile = null;
            String filePath = null;
            try {
                currentDoc = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
                VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
                String fileName = currentFile.getPath();
                psiFile = PsiDocumentManager.getInstance(project).getPsiFile(currentDoc);
                VirtualFile vFile = psiFile.getOriginalFile().getVirtualFile();
                filePath = vFile.getPath();
            }
            catch (NullPointerException ex) {
                return;
            }
            System.out.println(filePath);

            VirtualFile[] openedFiles = FileEditorManager.getInstance(project).getSelectedFiles();
            System.out.println(openedFiles);

            ConsoleView console = project.getUserData(PyCharmGCCStartup.CONSOLE_VIEW_KEY);
            if (console == null) {
                return;
            }
            ContentFactory contentFactory = ContentFactory.getInstance();
            Content content = contentFactory.createContent(console.getComponent(), "", true);
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("GCC Output");
            if (toolWindow == null) {
                return;
            }
            toolWindow.getContentManager().addContent(content);

            console.print(filePath, ConsoleViewContentType.NORMAL_OUTPUT);
            toolWindow.activate(null);
        }
    }
}

