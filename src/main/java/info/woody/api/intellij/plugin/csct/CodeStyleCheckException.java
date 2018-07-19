package info.woody.api.intellij.plugin.csct;

/**
 * Code style check exception.
 *
 * @author Woody
 */
public class CodeStyleCheckException extends RuntimeException {

    /**
     * Constructor.
     * @param message Exception message.
     */
    public CodeStyleCheckException(String message) {
        super(message);
    }
}
