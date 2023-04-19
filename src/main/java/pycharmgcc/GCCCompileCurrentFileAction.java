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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class GCCCompileCurrentFileAction extends AnAction {
    private Project thisProject = null;

    private String runGcc(PsiFile targetPsiFile, String outputName) {
        // Run gcc and save the resulting file in the same directory
        StringBuilder ret = new StringBuilder();
        VirtualFile vFile = targetPsiFile.getOriginalFile().getVirtualFile();
        String filepath = vFile.getPath();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("gcc", filepath, "-o", outputName);
            processBuilder.directory(new File(targetPsiFile.getContainingDirectory().getVirtualFile().getPath()));
            processBuilder.redirectErrorStream(true); // we want to be able to print errors
            Process process = processBuilder.start();

            // Read gcc's output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                ret.append(line).append("\n");
            }
            reader.close();

            // wait for gcc to finish running before retrieving the result
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                ret.append("Compilation succeeded.\n");
            } else {
                ret.append("Compilation failed with exit code: ").append(exitCode).append("\n");
            }
        } catch (IOException | InterruptedException ex) {
            ret.append("Error executing gcc command: ").append(ex.getMessage()).append("\n");
        }
        return ret.toString();
    }

    private void consoleWrite(String words) {
        // must be called after 'actionPerformed' is run
        // because actionPerformed sets up thisProject variable
        if (thisProject == null) {
            return;
        }
        ConsoleView console = thisProject.getUserData(PyCharmGCCStartup.CONSOLE_VIEW_KEY);
        ToolWindow window = ToolWindowManager.getInstance(thisProject).getToolWindow("GCC Output");
        if ((console == null) | (window == null)) {
            return;
        }
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(console.getComponent(), "", true);
        window.getContentManager().addContent(content);
        console.print(words, ConsoleViewContentType.NORMAL_OUTPUT);
        window.activate(null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        thisProject = event.getProject(); // user's project instance
        if (thisProject == null) {
            return;
        }
        Editor editor = FileEditorManager.getInstance(thisProject).getSelectedTextEditor(); // editor session
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
            currentDoc = FileEditorManager.getInstance(thisProject).getSelectedTextEditor().getDocument();
            VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
            String fileName = currentFile.getPath();
            psiFile = PsiDocumentManager.getInstance(thisProject).getPsiFile(currentDoc);
            VirtualFile vFile = psiFile.getOriginalFile().getVirtualFile();
            filePath = vFile.getPath();
        }
        catch (NullPointerException ex) {
            return;
        }
        System.out.println(filePath);

        VirtualFile[] openedFiles = FileEditorManager.getInstance(thisProject).getSelectedFiles();
        System.out.println(openedFiles);


        String curFileName = openedFiles[0].getName();
        String outname = curFileName.substring(0, curFileName.lastIndexOf('.')); // trim the filetype of the filename
        String cmdOut = runGcc(psiFile, outname);
        consoleWrite(cmdOut);
    }
}

