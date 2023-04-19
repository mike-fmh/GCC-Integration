package pycharmgcc;

import com.intellij.openapi.util.SystemInfo;
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
import org.apache.commons.lang3.tuple.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class GCCCompileCurrentFileAction extends AnAction {
    private Project thisProject = null;

    private Pair<Integer, String> runGcc(PsiFile targetPsiFile, String outputName) {
        // Run gcc and save the resulting file in the same directory
        Integer exitCode = 0;
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
            exitCode = process.waitFor();
            if (exitCode == 0) {
                ret.append("Compilation succeeded.\n");
            } else {
                ret.append("Compilation failed with exit code: ").append(exitCode).append("\n");
            }
        } catch (IOException | InterruptedException ex) {
            ret.append("Error executing gcc command: ").append(ex.getMessage()).append("\n");
        }
        return Pair.of(exitCode, ret.toString());
    }

    private void runExecutable(String exePath) {
        // run an executable and print to console while it's running
        consoleWrite("Running " + exePath + "\n");
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(exePath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // read the
                consoleWrite(line + "\n");
            }
            reader.close();

            // wait for gcc to finish running before retrieving the result
            int exitCode = process.waitFor();
            consoleWrite("Program finished with exit code: " + exitCode + "\n");

        } catch (IOException ex) {
            consoleWrite("Error while running program executable: " + ex.getMessage() + "\n");
        } catch (InterruptedException ex) {
            consoleWrite("Error while waiting for the process to finish: " + ex.getMessage() + "\n");
        }
    }

    private void clearConsole() {
        ConsoleView console = thisProject.getUserData(PyCharmGCCStartup.CONSOLE_VIEW_KEY);
        if (console == null) {
            return;
        }
        console.clear();
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

        VirtualFile[] openedFiles = FileEditorManager.getInstance(thisProject).getSelectedFiles();
        String curFileName = openedFiles[0].getName();
        String outname = null;
        String outpath = null;
        if (SystemInfo.isWindows) {
            // if it's a Windows system, executables should be .exe
            outname = curFileName.substring(0, curFileName.lastIndexOf('.')) + ".exe";
            outpath = filePath.substring(0, filePath.lastIndexOf('.')) + ".exe";
        }
        else {
            // otherwise just trim off the whole file type
            // in macOS & Linux, no file type usually means it's executable
            outname = curFileName.substring(0, curFileName.lastIndexOf('.'));
            outpath = filePath.substring(0, filePath.lastIndexOf('.'));
        }

        Pair<Integer, String> cmdRet = runGcc(psiFile, outname);
        Integer cmdCode = cmdRet.getLeft();
        String cmdOut = cmdRet.getRight();
        clearConsole();
        consoleWrite(cmdOut);

        if (cmdCode == 0) {
            consoleWrite("Saved compiled executable as " + outpath + "\n");
            runExecutable(outpath);
        }
    }
}
