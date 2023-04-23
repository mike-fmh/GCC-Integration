package gccintegration;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

public class GCCCompileCurrentFileAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project thisProject = event.getProject(); // user's project instance
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
            filePath = SysUtil.getCurrentFilepath(thisProject);
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
                SysUtil.clearConsole(thisProject);
                cmdRet = SysUtil.runCompiler(sourceFiles, outname, curFileType.equals("cpp"), thisProject);
            }

            if (cmdRet != null) {
                Integer cmdCode = cmdRet.getLeft();
                String cmdOut = cmdRet.getRight();

                SysUtil.consoleWrite(cmdOut, thisProject);

                if (cmdCode == 0) {
                    SysUtil.consoleWrite("Saved compiled executable as " + outpath + "\n", thisProject);
                    List<String> params = OptionParse.getChosenExeParams(thisProject, editor);
                    SysUtil.runExecutable(outpath, params, thisProject);
                }
            }
        }
    }
}
