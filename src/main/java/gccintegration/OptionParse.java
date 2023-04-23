package gccintegration;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPlainText;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class OptionParse {
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^//.*");
    // pattern must occur at the start of a line (^) because we want lines that are only comments
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("^\\s*$");

    /**
     * Return commented lines above all code of the active file
     */
    // is the lines only composed of spaces?
    public static List<String> getBeginComments(Project project, Editor editor) {
        // return commented lines above all code of the active file
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = editor.getDocument();
        PsiFile psiFile = psiDocumentManager.getPsiFile(document);

        List<String> comments = new ArrayList<>();

        if (psiFile != null) {
            for (PsiElement element : psiFile.getChildren()) {
                if (element instanceof PsiPlainText) {
                    String[] lines = element.getText().split("\\r?\\n");
                    // split file into a list of its lines
                    // \r is optional as sometimes windows will put \r\n to signify newlines
                    for (String line : lines) {
                        if (COMMENT_PATTERN.matcher(line).find()) {
                            // is the line a comment?
                            comments.add(line.trim().split("//")[1]); // remove the "//" from the comment
                        } else if (WHITESPACE_PATTERN.matcher(line).find()) {
                            // line is not a comment, but it's an empty line
                            // empty lines --> keep reading comments
                            continue;
                        }
                        else {
                            // non-comment line --> comments are over
                            break;
                        }
                    }
                }
            }
        }
        return comments;
    }

    /**
     * Return a list of params specified in the current file formatted
     * // [param1, param2, etc]
     */
    public static List<String> getChosenExeParams(Project project, Editor editor) {
        List<String> allComments = getBeginComments(project, editor);
        Pattern paramComment = Pattern.compile("\\s?\\[.*]");
        for (String comment : allComments) {
            if (paramComment.matcher(comment).find()) {
                // if the line matches "// [param1, param2, ...]
                // parse it and return

                // replace "[", "]", " ", with ""
                comment = comment.replace("[", "").replace("]", "").replace(" ", "");
                // transform the string "param1,param2" into a list
                // \\s*,\\s* splits along commas and ignores whitespaces
                return Arrays.asList(comment.split("\\s*,\\s*"));
            }
        }
        return new ArrayList<>();  // return nothing if user doesnt specify any params
    }

    /**
     * @return List of starting comments that specify source files
     * Must be formatted
     *
     * // source1, source2, etc
     *
     */
    public static List<String> getChosenSourceFiles(Project project, Editor editor, String mainSrcPath) {
        List<String> allComments = getBeginComments(project, editor);
        Pattern sourceComment = Pattern.compile("^(?!.*]$).*"); // match lines that don't end in "]"
        for (String comment : allComments) {
            if (sourceComment.matcher(comment).find()) {
                comment = comment.replace(" ", "");
                List<String> returnList = new ArrayList<>(Arrays.asList(comment.split("\\s*,\\s*")));

                // we need to remove the main source file from the "returnList" so we don't input it twice into gcc
                // if the user has included the main source file in their comment
                String[] mainSrcFilenames = mainSrcPath.split("/");
                String mainSrcFilename = mainSrcFilenames[mainSrcFilenames.length - 1];;
                returnList.remove(mainSrcFilename);

                returnList.add(0, mainSrcPath);
                return returnList;
            }
        }
        return new ArrayList<>();
    }
}
