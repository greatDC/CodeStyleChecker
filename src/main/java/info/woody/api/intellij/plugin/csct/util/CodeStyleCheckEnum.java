package info.woody.api.intellij.plugin.csct.util;

/**
 * Code style check enumeration types.
 * @author Woody
 */
public class CodeStyleCheckEnum {
    /**
     * Summary link type.
     */
    public enum SummaryLinkType {
        ISSUE, AUTHOR;
        public String make(String value) {
            return this.name().concat(Const.SIGN_HASH).concat(value);
        }
    }
}
