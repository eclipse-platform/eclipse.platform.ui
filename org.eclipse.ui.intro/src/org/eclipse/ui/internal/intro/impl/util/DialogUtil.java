/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.util;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.intro.impl.*;

/**
 * Utiliy class for Pop-Up dialogs. This is an NL enabled utility class (ie: you
 * do not have to NL enable the message before calling the methods here if you
 * are calling one of the methids that takes an id as a parameter).
 */

public class DialogUtil {

    /**
     * Displays core error dialog with a message from the Core error status
     * object, and a user message. <br>
     * The user message is retrieved from the errorID, and is logged using
     * Log.logError().
     */
    public static void displayCoreErrorDialog(final Shell parent,
            String errorId, CoreException coreEx) {

        String title = IntroPlugin.getResourceString("ErrorDialog.errorTitle");
        IStatus status = coreEx.getStatus();
        String msg = IntroPlugin.getResourceString(errorId);
        ErrorDialog.openError(parent, title, msg, status);
        Log.error(msg, coreEx);
    }

    /**
     * Displays error dialog with the given message. <br>
     */
    public static void displayErrorMessage(final Shell parent,
            final String msg, final Throwable ex) {

        String title = IntroPlugin
                .getResourceString("MessageDialog.errorTitle");
        MessageDialog.openError(parent, title, msg);
        Log.error(msg, ex);
    }

    /**
     * Displays error dialog with a message corresponding to the errorId. <br>
     * The user message is retrieved from the errorID, and is formatted with the
     * passed variables. Also logs the error using Log.logError().
     */
    public static void displayErrorMessage(final Shell parent,
            final String errorId, final Object[] variables, final Throwable ex) {

        String msg = null;
        if (variables != null)
            msg = IntroPlugin.getFormattedResourceString(errorId, variables);
        else
            msg = IntroPlugin.getResourceString(errorId);
        displayErrorMessage(parent, msg, ex);
    }

    /**
     * Displays warning dialog with a given message. <br>
     * also logs the info using Log.logWarning(). msg error message to display
     * and log.
     */
    public static void displayWarningMessage(final Shell parent,
            final String msg) {

        String title = IntroPlugin
                .getResourceString("MessageDialog.warningTitle");
        MessageDialog.openWarning(parent, title, msg);
        Log.warning(msg);
    }

    /**
     * Displays warning dialog with a message corresponding to the errorId. <br>
     * also logs the info using Log.logWarning().
     */
    public static void displayWarningMessage(final Shell parent,
            final String warningId, final Object[] variables) {

        String msg = null;
        if (variables != null)
            msg = IntroPlugin.getFormattedResourceString(warningId, variables);
        else
            msg = IntroPlugin.getResourceString(warningId);
        displayWarningMessage(parent, msg);

    }

    /**
     * Displays info dialog with a message corresponding to the infoId. <br>
     * also logs the info using Log.logInfo().
     */
    public static void displayInfoMessage(final Shell parent, final String msg) {

        String title = IntroPlugin.getResourceString("MessageDialog.infoTitle");
        MessageDialog.openInformation(parent, title, msg);
        Log.info(msg);

    }

    /**
     * Displays info dialog with a message corresponding to the infoId. <br>
     * also logs the info using Log.logInfo().
     */
    public static void displayInfoMessage(final Shell parent,
            final String infoId, final Object[] variables) {

        String msg = null;
        if (variables != null)
            msg = IntroPlugin.getFormattedResourceString(infoId, variables);
        else
            msg = IntroPlugin.getResourceString(infoId);
        displayInfoMessage(parent, msg);
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }

    /**
     * Utility method to best find the active shell.
     */
    public static Shell getActiveShell() {
        Display display = getCurrentDisplay();
        Shell activeShell = display.getActiveShell();
        if (activeShell == null)
            return getActiveWorkbenchWindow().getShell();
        else
            return activeShell;
    }

    /**
     * Utility method to best find the active Display.
     */
    public static Display getCurrentDisplay() {
        Display display = Display.getCurrent();
        if (display != null)
            return display;
        else
            return Display.getDefault();
    }

}