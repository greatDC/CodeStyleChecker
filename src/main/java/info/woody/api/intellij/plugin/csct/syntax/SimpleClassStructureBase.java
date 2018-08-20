package info.woody.api.intellij.plugin.csct.syntax;

import java.util.List;

abstract class SimpleClassStructureBase {
    protected String meta;
    protected int lineStartIndex;
    protected int lineEndIndex;
    protected SimpleClassStructureType type;
    protected List<SimpleClassStructureReference> referenceList;

    /**
     * Constructor.
     */
    public SimpleClassStructureBase(SimpleClassStructureType type) {
        this.type = type;
    }

    public List<SimpleClassStructureReference> getReferenceList() {
        return referenceList;
    }

    public void setReferenceList(List<SimpleClassStructureReference> referenceList) {
        this.referenceList = referenceList;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public int getLineStartIndex() {
        return lineStartIndex;
    }

    public void setLineStartIndex(int lineStartIndex) {
        this.lineStartIndex = lineStartIndex;
    }

    public int getLineEndIndex() {
        return lineEndIndex;
    }

    public void setLineEndIndex(int lineEndIndex) {
        this.lineEndIndex = lineEndIndex;
    }

    public SimpleClassStructureType getType() {
        return type;
    }

    public void setType(SimpleClassStructureType type) {
        this.type = type;
    }
}
