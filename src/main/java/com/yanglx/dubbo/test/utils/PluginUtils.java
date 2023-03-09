package com.yanglx.dubbo.test.utils;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.ui.tabs.TabInfo;
import com.yanglx.dubbo.test.CacheInfo;
import com.yanglx.dubbo.test.DubboSetingState;
import com.yanglx.dubbo.test.PluginConstants;
import com.yanglx.dubbo.test.dubbo.DubboMethodEntity;
import com.yanglx.dubbo.test.ui.DubboPanel;
import com.yanglx.dubbo.test.ui.Tab;
import com.yanglx.dubbo.test.ui.TabBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * <p>Description: </p>
 *
 * @author yanglx
 * @version 1.0.0
 * @email "mailto:dev_ylx@163.com"
 * @date 2021.02.20 15:57
 * @since 1.0.0
 */

public class PluginUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginUtils.class);

    /**
     * 写入文本
     *
     * @param project  project
     * @param document document
     * @param text     text
     * @since 1.0.0
     */
    public static void writeDocument(Project project, Document document, String text) {
        WriteCommandAction.runWriteCommandAction(project, PluginConstants.PLUGIN_NAME, PluginConstants.PLUGIN_NAME,
                () -> document.setText(text));
    }

    /**
     * element 只有是 method 或者是 method 引用时才可用.
     *
     * @param project project
     * @param editor  editor
     * @param element element
     * @return the boolean
     * @since 1.1.0
     */
    public static boolean isAvailable(Project project,
                                      Editor editor,
                                      PsiElement element) {
        if (project == null || editor == null) {
            return false;
        }

        return getPsiMethod(element) != null;
    }

    /**
     * 解析当前光标选中的元素, 返回 PsiMethod.
     *
     * @param element element
     * @since 1.1.0
     */
    public static PsiMethod getPsiMethod(PsiElement element) {
        PsiMethod method = null;
        if (element instanceof PsiMethod) {
            method = (PsiMethod) element;
        } else if (element instanceof PsiIdentifier) {
            // 如果标识符是方法
            if (element.getParent() instanceof PsiMethod) {
                method = (PsiMethod) element.getParent();
            } else if (element.getParent() instanceof PsiReferenceExpression) {
                // 如果标识符是方法引用
                PsiElement resolve = ((PsiReferenceExpression) element.getParent()).resolve();
                if (resolve instanceof PsiMethod) {
                    method = (PsiMethod) resolve;
                }
            }
        }

        return method;
    }

    /**
     * Open tool window
     *
     * @param project project
     * @param element element
     * @since 1.1.0
     */
    public static void openToolWindow(Project project,
                                      PsiElement element) {
        PsiMethod psiMethod = getPsiMethod(element);

        PsiParameterList parameterList = psiMethod.getParameterList();
        PsiJavaFile javaFile = (PsiJavaFile) psiMethod.getContainingFile();
        PsiClass psiClass = (PsiClass) psiMethod.getParent();

        // 服务名称
        String interfaceName = String.format("%s.%s", javaFile.getPackageName(), psiClass.getName());
        // 入参类型
        String[] methodType = new String[parameterList.getParameters().length];
        for (int i = 0; i < parameterList.getParameters().length; i++) {
            String canonicalText = StrUtils.trimClassName(parameterList.getParameters()[i].getType().getCanonicalText());
            methodType[i] = canonicalText;
        }
        // 入参
        Object[] initParamArray = ParamUtil.getInitParamArray(psiMethod.getParameterList(), psiMethod.getDocComment());
        // 接口名称
        String methodName = psiMethod.getName();

        ToolWindow toolWindow = ToolWindowManager
                .getInstance(Objects.requireNonNull(project))
                .getToolWindow(PluginConstants.PLUGIN_NAME);
        if (toolWindow != null) {
            // 无论当前状态为关闭/打开，进行强制打开 ToolWindow
            IntellijUtils.safelyInvokeLater(() -> toolWindow.show(null));
        }

        TabInfo selectedInfo = TabBar.getSelectionTabInfo();
        Tab component = (Tab) selectedInfo.getComponent();
        DubboSetingState settings = DubboSetingState.getInstance();
        List<CacheInfo> dubboConfigs = settings.getDubboConfigs();
        CacheInfo defaultSetting = dubboConfigs.get(0);
        DubboMethodEntity dubboMethodEntity = new DubboMethodEntity();
        dubboMethodEntity.setAddress(defaultSetting.getAddress());
        dubboMethodEntity.setVersion(defaultSetting.getVersion());
        dubboMethodEntity.setGroup(defaultSetting.getGroup());
        dubboMethodEntity.setInterfaceName(interfaceName);
        dubboMethodEntity.setParam(initParamArray);
        dubboMethodEntity.setMethodType(methodType);
        dubboMethodEntity.setMethodName(methodName);
        dubboMethodEntity.setId(defaultSetting.getId());
        IntellijUtils.safelyInvokeLater(() -> DubboPanel.refreshUI(component.getDubboPanel(), dubboMethodEntity));
    }

}
