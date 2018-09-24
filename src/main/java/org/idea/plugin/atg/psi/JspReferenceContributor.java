package org.idea.plugin.atg.psi;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import org.idea.plugin.atg.Constants;
import org.idea.plugin.atg.psi.reference.JspFileReference;
import org.idea.plugin.atg.psi.reference.WebContextResourceReference;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class JspReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiPlainTextFile.class), new JspReferenceProvider());

    }

    static class JspReferenceProvider extends PsiReferenceProvider {
        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                     @NotNull ProcessingContext
                                                             context) {
            if (element instanceof PsiPlainTextFile) {
                PsiPlainTextFile jspFile = (PsiPlainTextFile) element;
                VirtualFile virtualFile = jspFile.getVirtualFile();
                if (virtualFile != null) {
                    String filePath = virtualFile.getCanonicalPath();
                    if (filePath != null && filePath.endsWith(".jsp")) {
                        PsiFile jspPsiFile = PsiFileFactory.getInstance(jspFile.getProject()).createFileFromText(XMLLanguage.INSTANCE, element.getText());
                        JspVisitor jspVisitor = new JspVisitor(jspFile);
                        jspPsiFile.accept(jspVisitor);
                        return jspVisitor.getCreatedReferences().toArray(new PsiReference[0]);
                    }
                }
            }
            return PsiReference.EMPTY_ARRAY;
        }
    }

    static class JspVisitor extends PsiRecursiveElementWalkingVisitor {
        private final PsiPlainTextFile originalFile;
        private final Map<String, String> activeImports = new HashMap<>();
        private final List<PsiReference> createdReferences = new ArrayList<>();
        private final List<String> tagsContainingBeanAttribute = Arrays.asList("importbean", "param", "getvalueof", "a", "select", "input", "valueof", "tomap", "textarea", "setvalue", "property", "postfield");
        private final List<String> tagsContainingBeanValueAttribute = Arrays.asList("setvalue", "option", "property", "postfield", "param");

        JspVisitor(PsiPlainTextFile originalFile) {
            this.originalFile = originalFile;
        }

        @Override
        public void visitElement(PsiElement element) {
            if (element instanceof XmlTag) {
                XmlTag xmlTag = (XmlTag) element;
                String fullTageName = xmlTag.getName();
                String tagNameWithoutNameSpace = fullTageName.contains(":") ? fullTageName.substring(fullTageName.indexOf(':') + 1) : fullTageName;
                if (Constants.Keywords.IMPORT_BEAN_TAG.equals(tagNameWithoutNameSpace)) {
                    XmlAttribute bean = xmlTag.getAttribute(Constants.Keywords.BEAN_ATTRIBUTE);
                    if (bean != null) {
                        XmlAttributeValue beanValue = bean.getValueElement();
                        if (beanValue != null) {
                            String beanName = beanValue.getValue() != null ? beanValue.getValue() : "";
                            String shortBeanName = beanName.contains("/") ? beanName.substring(beanName.lastIndexOf('/') + 1) : beanName;
                            activeImports.put(shortBeanName, beanName);
                        }
                    }
                }
                if (Constants.Keywords.DROPLET_TAG.equals(tagNameWithoutNameSpace)) {
                    XmlAttribute bean = xmlTag.getAttribute(Constants.Keywords.NAME_ATTRIBUTE);
                    if (bean != null) {
                        XmlAttributeValue beanValue = bean.getValueElement();
                        if (beanValue != null) {
                            createdReferences.addAll(AtgComponentReferenceCreator.createReferences(beanValue, originalFile, activeImports));
                        }
                    }
                }
                if (tagsContainingBeanAttribute.contains(tagNameWithoutNameSpace)) {
                    XmlAttribute bean = xmlTag.getAttribute(Constants.Keywords.BEAN_ATTRIBUTE);
                    if (bean != null) {
                        XmlAttributeValue beanValue = bean.getValueElement();
                        if (beanValue != null) {
                            createdReferences.addAll(AtgComponentReferenceCreator.createReferences(beanValue, originalFile, activeImports));
                        }
                    }
                }
                if (tagsContainingBeanValueAttribute.contains(tagNameWithoutNameSpace)) {
                    XmlAttribute bean = xmlTag.getAttribute(Constants.Keywords.BEAN_VALUE_ATTRIBUTE);
                    if (bean != null) {
                        XmlAttributeValue beanValue = bean.getValueElement();
                        if (beanValue != null) {
                            createdReferences.addAll(AtgComponentReferenceCreator.createReferences(beanValue, originalFile, activeImports));
                        }
                    }
                }
                if (Constants.Keywords.INCLUDE_TAG.equals(tagNameWithoutNameSpace)) {
                    XmlAttribute includeFile = xmlTag.getAttribute(Constants.Keywords.PAGE_ATTRIBUTE);
                    if (includeFile != null) {
                        XmlAttributeValue pageValue = includeFile.getValueElement();
                        if (pageValue != null && pageValue.getValue() != null) {
                            TextRange valueTextRange = pageValue.getValueTextRange();
                            createdReferences.add(new JspFileReference(pageValue.getValue(), originalFile, valueTextRange));
                        }
                    }
                }

                if (Constants.Keywords.IMG_TAG.equals(tagNameWithoutNameSpace) || Constants.Keywords.SCRIPT_TAG.equals(tagNameWithoutNameSpace)) {
                    XmlAttribute includeFile = xmlTag.getAttribute(Constants.Keywords.SRC_ATTRIBUTE);
                    if (includeFile != null) {
                        XmlAttributeValue pageValue = includeFile.getValueElement();
                        if (pageValue != null && pageValue.getValue() != null) {
                            TextRange valueTextRange = pageValue.getValueTextRange();
                            createdReferences.add(new WebContextResourceReference(pageValue.getValue(), originalFile, valueTextRange));
                        }
                    }
                }
            }
            super.visitElement(element);
        }

        @NotNull
        public List<PsiReference> getCreatedReferences() {
            return createdReferences;
        }
    }
}