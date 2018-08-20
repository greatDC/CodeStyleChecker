package info.woody.api.intellij.plugin.csct.syntax;

import java.util.ArrayList;
import java.util.List;

public class SimpleClassStructureMethod extends SimpleClassStructureBase {
    private List<SimpleClassStructureStatement> statementList = new ArrayList<>();

    /**
     * Constructor.
     */
    public SimpleClassStructureMethod() {
        super(SimpleClassStructureType.METHOD);
    }

    public List<SimpleClassStructureStatement> getStatementList() {
        return statementList;
    }

    public void setStatementList(List<SimpleClassStructureStatement> statementList) {
        this.statementList = statementList;
    }
}
