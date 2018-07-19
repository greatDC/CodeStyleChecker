package info.woody.api.intellij.plugin.csct.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Paths;

/**
 * Editor utility class.
 *
 * @author Woody
 */
public class EditorUtils {

    /**
     * Open file in the editor.
     *
     * @param path File path.
     * @param project Project.
     */
    public static Editor openFileInEditor(String path, Project project) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(Paths.get(path).toFile());
        if (null != virtualFile && virtualFile.isValid()) {
            FileEditorProvider[] providers = FileEditorProviderManager.getInstance().getProviders(project, virtualFile);
            if (providers.length > 0) {
                return FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, virtualFile), false);
            }
        }
        return null;
    }
}
