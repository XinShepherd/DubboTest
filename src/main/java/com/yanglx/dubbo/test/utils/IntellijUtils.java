package com.yanglx.dubbo.test.utils;

import com.intellij.openapi.application.ApplicationManager;

/**
 * @author Fuxin
 */
public class IntellijUtils {

    public static void safelyInvokeLater(Runnable runnable) {
        try {
            ApplicationManager.getApplication().invokeLater(runnable);
        } catch (Exception ignored) {
        }
    }

}
