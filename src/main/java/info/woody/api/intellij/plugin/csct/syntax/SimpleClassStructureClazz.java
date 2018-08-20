package info.woody.api.intellij.plugin.csct.syntax;

import java.util.ArrayList;
import java.util.List;

public class SimpleClassStructureClazz extends SimpleClassStructureBase {

    private List<SimpleClassStructureImport> importList = new ArrayList<>();
    private List<SimpleClassStructureMethod> methodList = new ArrayList<>();

    /**
     * Constructor.
     */
    public SimpleClassStructureClazz() {
        super(SimpleClassStructureType.CLAZZ);
    }

    public List<SimpleClassStructureImport> getImportList() {
        return importList;
    }

    public void setImportList(List<SimpleClassStructureImport> importList) {
        this.importList = importList;
    }

    public List<SimpleClassStructureMethod> getMethodList() {
        return methodList;
    }

    public void setMethodList(List<SimpleClassStructureMethod> methodList) {
        this.methodList = methodList;
    }
}
