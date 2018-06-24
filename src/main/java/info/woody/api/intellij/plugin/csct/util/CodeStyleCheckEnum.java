package info.woody.api.intellij.plugin.csct.util;

public class CodeStyleCheckEnum {
    public static enum SummaryLinkType {
        ISSUE, AUTHOR;
        public String make(String value) {
            return this.name().concat("#").concat(value);
        }


    }
}
