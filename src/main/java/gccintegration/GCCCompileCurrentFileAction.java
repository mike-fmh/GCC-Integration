package gccintegration;

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
import java.util.ArrayList;
import java.util.List;

public class GCCCompileCurrentFileAction extends AnAction {
    private Project thisProject = null;

    private String getCurrentFile(Project project) {
        // GET CURRENT FILE's PATH
        // we need to get the current file as a
        // Document -> PsiFile -> String
        // to get the absolute filepath
        Document currentDoc = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(currentDoc);
        VirtualFile vFile = psiFile.getOriginalFile().getVirtualFile();
        return vFile.getPath();
    }

    /**
     * Isolate the filename from filepaths
     * @param filePaths String List of full filepaths
     * @return String List of filenames
     */
    private List<String> getFileNames(List<String> filePaths) {
        List<String> fileNames = new ArrayList<>();
        for (String file : filePaths) {
            fileNames.add(getFileName(file));
        }
        return fileNames;
    }

    private String getFileName(String file) {
        if (file.contains("/")) {
            // isolate the file's name
            String[] filePath = file.split("/");
            return filePath[filePath.length - 1];
        }
        else {
            return file;
        }
    }

    /**
     *
     * @param sourceFiles the source files to compile
     * @param outputName the name of the output executable
     * @param cpp Is it a .cpp file? (default .c)
     * @return Pair of int, string (return code, std output)
     */
    private Pair<Integer, String> runCompiler(List<String> sourceFiles, String outputName, Boolean cpp) {
        consoleWrite((cpp ? "Compiling using G++ " : "Compiling using GCC ") + sourceFiles + "\n");
        Integer exitCode = 0;
        StringBuilder ret = new StringBuilder();
        String mainSrcPath = sourceFiles.get(0);
        sourceFiles.remove(0);
        sourceFiles.add(0, getFileName(mainSrcPath));
        File workingDir = new File(mainSrcPath).getParentFile();
        sourceFiles.add(0, (cpp ? "g++" : "gcc"));
        sourceFiles.add("-o");
        sourceFiles.add(outputName);

        // convert the full command list to a string for printing
        String fullCmdString = String.join(" ", sourceFiles);
        consoleWrite("% " + fullCmdString + "\n");

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(sourceFiles);
            processBuilder.directory(workingDir);
            processBuilder.redirectErrorStream(true); // we want to be able to print errors
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                ret.append(line).append("\n");
            }
            reader.close();
            exitCode = process.waitFor();
            if (exitCode == 0) {
                ret.append("Compilation succeeded.\n");
            } else {
                ret.append("Compilation failed with exit code: ").append(exitCode).append("\n");
            }
        } catch (IOException | InterruptedException ex) {
            ret.append("Error executing ").append(cpp ? "g++" : "gcc").append(" command: ").append(ex.getMessage()).append("\n");
        }
        return Pair.of(exitCode, ret.toString());
    }

    private void runExecutable(String exePath, List<String> params) {
        // run an executable and print to console while it's running
        try {
            File exeFile = new File(exePath);
            File workingDirectory = exeFile.getParentFile();
            List<String> fullCmd = new ArrayList<>(params);

            String fileName = getFileName(exePath);
            // add the exe file to the beginning of the full command
            fullCmd.add(0, "./" + fileName);
            // convert the full command list to a string for printing
            String fullCmdString = String.join(" ", fullCmd);

            consoleWrite("Running with parameters: " + params + "\n% " + fullCmdString + "\n");
            ProcessBuilder processBuilder = new ProcessBuilder(fullCmd);
            processBuilder.directory(workingDirectory);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // read the
                consoleWrite("\t" + line + "\n");
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
        ConsoleView console = thisProject.getUserData(ProjectStartup.CONSOLE_VIEW_KEY);
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
        ConsoleView console = thisProject.getUserData(ProjectStartup.CONSOLE_VIEW_KEY);
        ToolWindow window = ToolWindowManager.getInstance(thisProject).getToolWindow("GCC/G++ Output");
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



        VirtualFile[] openedFiles = FileEditorManager.getInstance(thisProject).getSelectedFiles();
        String curFileName = openedFiles[0].getName();
        String outname = null;
        String outpath = null;

        // retrieve the file type of the active file
        int lastIndex = curFileName.lastIndexOf('.');
        String curFileType = null;
        if (lastIndex > 0) {
            curFileType = curFileName.substring(lastIndex + 1);
        }
        String filePath = null;
        try {
            filePath = getCurrentFile(thisProject);
        }
        catch (NullPointerException ex) {
            return;
        }
        if (curFileType != null) {
            if (SystemInfo.isWindows) {
                // if it's a Windows system, executables should be .exe
                outname = curFileName.substring(0, curFileName.lastIndexOf('.')) + ".exe";
                outpath = filePath.substring(0, filePath.lastIndexOf('.')) + ".exe";
            } else {
                // otherwise just trim off the whole file type
                // in macOS & Linux, no file type usually means it's executable
                outname = curFileName.substring(0, curFileName.lastIndexOf('.'));
                outpath = filePath.substring(0, filePath.lastIndexOf('.'));
            }

            // determine if we need to use GCC or G++ (.c or .cpp)
            Pair<Integer, String> cmdRet = null;
            if ((curFileType.equals("c")) | (curFileType.equals("cpp"))) {
                List<String> sourceFiles = OptionParse.getChosenSourceFiles(thisProject, editor, filePath);
                clearConsole();
                cmdRet = runCompiler(sourceFiles, outname, curFileType.equals("cpp"));
            }

            if (cmdRet != null) {
                Integer cmdCode = cmdRet.getLeft();
                String cmdOut = cmdRet.getRight();

                consoleWrite(cmdOut);

                if (cmdCode == 0) {
                    consoleWrite("Saved compiled executable as " + outpath + "\n");
                    List<String> params = OptionParse.getChosenExeParams(thisProject, editor);
                    runExecutable(outpath, params);
                }
            }
        }
    }
}
