package org.idea.plugin.atg.index;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.idea.plugin.atg.visitor.XmlPsiRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XmlActorIndexExtension extends FileBasedIndexExtension<String, String> {

    // FIXME: 12/23/2019 What purpose of given index?
    //  Every additional index slows down when any file changes(just one symbol typed), as all indexes are considered for being rebuilt.
    //  At this moment I see, you just use it as list of ATG-related Xml files (by standard it should be ATG config folders) which have specific tag.
    //  As you anyway invokes parsing of all these files I'd recommend to use replace this index with AtgXmlsIndexExtension
    //  (which already indexes all XMLs located under Config/ConfigLayer folders) + additional filter of top XML tag.
    //  There shouldn't be huge amount of XML files
    //  PS. I don't understand purpose of storing fileName/path there.

    private static final Logger LOG = Logger.getInstance(XmlActorIndexExtension.class);
    private static final String ACTOR_TEMPLATE_XML_TAG = "actor-template";
    public static final ID<String, String> NAME = ID.create("atgXmlActors");


    @NotNull
    @Override
    public ID<String, String> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            VirtualFile file = inputData.getFile();
            // FIXME: 12/23/2019  Storing in index filePath/fileName is redundant as each entry maps to particular file
            return Collections.singletonMap(file.getName(), file.getPath());
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter(XmlFileType.INSTANCE) {
            private Project project = ProjectManager.getInstance().getDefaultProject();
            private PsiManager psiManager = PsiManager.getInstance(project);

            @Override
            public boolean acceptInput(@NotNull VirtualFile file) {
                boolean foundActorTag = false;
                if(isXmlFile(file)) {
                    PsiFile psiFile = psiManager.findFile(file);
                    if (psiFile != null) {
                        PsiElement psiElement = psiFile.getOriginalElement();
                        List<PsiElement> xmlTags = new ArrayList<>();
                        PsiElementVisitor elementVisitor =
                                new XmlPsiRecursiveElementVisitor(ACTOR_TEMPLATE_XML_TAG, xmlTags);
                        elementVisitor.visitElement(psiElement);
                        foundActorTag = !xmlTags.isEmpty();
                    }
                }
                return foundActorTag;
            }
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    private boolean isXmlFile(@NotNull VirtualFile fileOrDir) {
        return XmlFileType.DEFAULT_EXTENSION.equals(fileOrDir.getExtension());
    }
}
