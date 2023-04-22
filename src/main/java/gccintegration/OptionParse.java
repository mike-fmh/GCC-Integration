package gccintegration;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPlainText;
import java.util.ArrayList;
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
}
