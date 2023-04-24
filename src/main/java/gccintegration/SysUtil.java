package gccintegration;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.apache.commons.lang3.tuple.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import com.intellij.openapi.util.Key;
import java.util.ArrayList;
import java.util.List;

public class SysUtil {
    private static final Key<ConsoleView> CONSOLE_VIEW_KEY = new com.intellij.openapi.util.Key<>("gccConsole.ConsoleView");

    public static ConsoleView getStoredConsole(Project project) {
        ConsoleView console = project.getUserData(CONSOLE_VIEW_KEY);
        if (console == null) {
            project.putUserData(CONSOLE_VIEW_KEY, new ConsoleViewImpl(project, true));
            console = project.getUserData(CONSOLE_VIEW_KEY);
        }
        return console;
    }

    public static void clearConsole(Project project) {
        ConsoleView console = getStoredConsole(project);
        console.clear();
    }

    public static void consoleWrite(String words, Project project) {
        // must be called after 'actionPerformed' is run
        // because actionPerformed sets up thisProject variable

        ConsoleView console = getStoredConsole(project);
        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("GCC/G++ Output");
        if (window == null) {
            return;
        }
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(console.getComponent(), "", true);
        window.getContentManager().addContent(content);
        console.print(words, ConsoleViewContentType.NORMAL_OUTPUT);
        window.activate(null);
    }

    /**
     * @param project IDE's opened project
     * @return filepath of currently open file in the IDE editor
     */
    public static String getCurrentFilepath(Project project) {
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
    public static List<String> getFileNames(List<String> filePaths) {
        List<String> fileNames = new ArrayList<>();
        for (String file : filePaths) {
            fileNames.add(getFileName(file));
        }
        return fileNames;
    }

    /**
     *
     * @param filepath String filepath
     * @return the filename referenced by the filepath
     */
    public static String getFileName(String filepath) {
        if (filepath.contains("/")) {
            // isolate the file's name
            String[] filePathList = filepath.split("/");
            return filePathList[filePathList.length - 1];
        }
        else {
            return filepath;
        }
    }

    /**
     *
     * @param sourceFiles the source files to compile
     * @param outputName the name of the output executable
     * @param cpp Is it a .cpp file? (default .c)
     * @return Pair of int, string (return code, std output)
     */
    public static Pair<Integer, String> runCompiler(List<String> sourceFiles, String outputName, Boolean cpp, Project project) {
        consoleWrite((cpp ? "Compiling using G++ " : "Compiling using GCC ") + sourceFiles + "\n", project);
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
        consoleWrite("% " + fullCmdString + "\n", project);

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

    public static void runExecutable(String exePath, List<String> params, Project project) {
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

            consoleWrite("Running with parameters: " + params + "\n% " + fullCmdString + "\n", project);
            ProcessBuilder processBuilder = new ProcessBuilder(fullCmd);
            processBuilder.directory(workingDirectory);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // read the
                consoleWrite("\t" + line + "\n", project);
            }
            reader.close();
            // wait for gcc to finish running before retrieving the result
            int exitCode = process.waitFor();
            consoleWrite("Program finished with exit code: " + exitCode + "\n", project);

        } catch (IOException ex) {
            consoleWrite("Error while running program executable: " + ex.getMessage() + "\n", project);
        } catch (InterruptedException ex) {
            consoleWrite("Error while waiting for the process to finish: " + ex.getMessage() + "\n", project);
        }
    }
}
