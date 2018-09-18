package org.idea.plugin.atg.navigation;

import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import com.intellij.psi.util.PsiUtilBase;
import org.idea.plugin.atg.AtgToolkitBundle;
import org.idea.plugin.atg.config.AtgToolkitConfig;
import org.idea.plugin.atg.util.AtgComponentUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class GoToComponentCodeAction extends BaseCodeInsightAction {
    @Override
    @NotNull
    protected GoToComponentTargetHandler getHandler() {
        return new GoToComponentTargetHandler();
    }

    @Override
    public void update(AnActionEvent e) {
        final Presentation presentation = e.getPresentation();
        presentation.setEnabledAndVisible(false);

        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (editor == null || project == null) return;

        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (psiFile instanceof PsiJavaFile) {
            Optional<PsiClass> srcClass = Arrays.stream(((PsiJavaFile) psiFile).getClasses())
                    .filter(AtgComponentUtil::isApplicableToHaveComponents)
                    .findAny();
            if (srcClass.isPresent()) {
                presentation.setText(AtgToolkitBundle.message("goto.component.from.class.text"));
                presentation.setDescription(AtgToolkitBundle.message("goto.component.from.class.description"));
                presentation.setEnabledAndVisible(true);
            }
        } else if (psiFile instanceof PropertiesFile) {
            presentation.setText(AtgToolkitBundle.message("goto.component.from.component.text"));
            presentation.setDescription(AtgToolkitBundle.message("goto.component.from.component.description"));
            if (AtgToolkitConfig.getInstance(project).showReferencesOnComponentInGoTo || AtgToolkitConfig.getInstance(project).showOverridesOfComponentInGoTo) {
                presentation.setEnabledAndVisible(true);
            }
        } else if (psiFile instanceof XmlFileImpl) {
            presentation.setText(AtgToolkitBundle.message("goto.component.from.component.text"));
            presentation.setDescription(AtgToolkitBundle.message("goto.component.from.component.description"));
            if (AtgToolkitConfig.getInstance(project).showOverridesOfComponentInGoTo) {
                presentation.setEnabledAndVisible(true);
            }
        }
    }
}
