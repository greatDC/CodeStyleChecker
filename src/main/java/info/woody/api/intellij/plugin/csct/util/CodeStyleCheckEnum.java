package info.woody.api.intellij.plugin.csct.util;

public class CodeStyleCheckEnum {
    public enum SummaryLinkType {
        ISSUE, AUTHOR;
        public String make(String value) {
            return this.name().concat(Const.SIGN_HASH).concat(value);
        }
    }
}
