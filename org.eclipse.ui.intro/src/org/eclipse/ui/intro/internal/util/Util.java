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
package org.eclipse.ui.intro.internal.util;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.intro.internal.*;

public class Util {

    private static String newLineSeparator = null;

    /**
     * Handle the exception by logging to the Logger. <br>
     * The errorId is used to NL enable the error message. Pass
     * <code>null</code> as the messageId to indicate that the error's message
     * should be shown as the primary message
     */
    public static void handleException(String errorId, Exception e) {
        handleException(errorId, e, null);
    }

    /**
     * Handle the exception by logging to the Logger. <br>
     * The errorId is used to NL enable the error message, and the variables are
     * subsituted in the message. Pass <code>null</code> as the messageId to
     * indicate that the error's message should be shown as the primary message
     */
    public static void handleException(String errorId, Exception e,
            Object[] variables) {
        String msg = null;
        if (variables != null) {
            // if variables is not null, errorId will never be null.
            msg = IntroPlugin.getFormattedResourceString(errorId, variables);
        } else {
            if (errorId == null)
                msg = e.getMessage();
            else
                msg = IntroPlugin.getResourceString(errorId);
        }
        Logger.logError(msg, e);
    }

    /**
     * Handle the exception by displaying an Error Dialog. <br>
     * The errorId is used to NL enable the error message. Also, the error is
     * logged by the Logger. Pass <code>null</code> as the messageId to
     * indicate that the error's message should be shown as the primary message
     */
    public static void handleExceptionWithPopUp(Shell parent, String errorId,
            Exception e) {
        // if it is a core exception, use ErrorDialog. If the error id is null
        // this translates to giving null to this dialog which is handled by
        // Eclipse by displaying the detyailed message directly.
        if (e instanceof CoreException) {
            if (parent == null)
                parent = DialogUtil.getActiveShell();
            DialogUtil.displayCoreErrorDialog(parent, errorId,
                    (CoreException) e);

            return;
        }
        // any other exception, use MessageDialog.
        // if errorID is null, use error message.
        if (errorId == null)
            errorId = e.getMessage();
        if (parent == null)
            parent = DialogUtil.getActiveShell();
        DialogUtil.displayErrorMessage(parent, errorId, e);
    }

    public static void printLayout(String text, Layout layout) {
        if (layout == null) {
            System.out.println(text + ": NULL Layout object.");
            return;
        }
        if (layout instanceof FillLayout)
            printLayout(text, (FillLayout) layout);
        else
            System.out.println(text + ": " + layout.toString());
    }

    public static void printLayout(String text, FillLayout layout) {
        System.out.print(text + ": ");
        System.out.print("FillLayout object:");
        String type = "horizontal";
        if (layout.type == SWT.VERTICAL)
            type = "vertical";
        System.out.print(" type: " + type);
        System.out.print(" margin height: " + layout.marginHeight);
        System.out.print(" margin width: " + layout.marginWidth);
        System.out.println(" spacing: " + layout.spacing);
    }

    /**
     * Utility method that will add a debug listener to the given control. All
     * common events are added.
     * 
     * @param control
     * @return
     */
    public static Listener addDebugListener(Control control) {
        Listener listener = new Listener() {

            public void handleEvent(Event e) {
                switch (e.type) {
                case SWT.Selection:
                    System.out.println("Selection EVENT: " + e.toString());
                    break;
                case SWT.Dispose:
                    System.out.println("Dispose EVENT: " + e.toString());
                    break;
                case SWT.Paint:
                    System.out.println("Paint EVENT: " + e.toString());
                    break;
                case SWT.Resize:
                    System.out.println("Resize EVENT: " + e.toString());
                    break;
                case SWT.MouseDoubleClick:
                    System.out.println("MouseDoubleClick EVENT: "
                            + e.toString());
                    break;
                case SWT.MouseDown:
                    System.out.println("MouseDown EVENT: " + e.toString());
                    break;
                case SWT.MouseUp:
                    System.out.println("MouseUp EVENT: " + e.toString());
                    break;
                case SWT.MouseMove:
                    System.out.println("MouseMove EVENT: " + e.toString());
                    break;
                case SWT.MouseEnter:
                    System.out.println("MouseEnter EVENT: " + e.toString());
                    break;
                case SWT.MouseExit:
                    System.out.println("MouseExit EVENT: " + e.toString());
                    break;
                case SWT.MouseHover:
                    System.out.println("MouseHover EVENT: " + e.toString());
                    break;
                case SWT.FocusIn:
                    System.out.println("FocusIn EVENT: " + e.toString());
                    break;
                case SWT.FocusOut:
                    System.out.println("FocusOut EVENT: " + e.toString());
                    break;
                case SWT.KeyDown:
                    System.out.println("KeyDown EVENT: " + e.toString());
                    break;
                case SWT.KeyUp:
                    System.out.println("KeyUp EVENT: " + e.toString());
                    break;
                case SWT.Traverse:
                    System.out.println("Traverse EVENT: " + e.toString());
                    break;
                case SWT.Show:
                    System.out.println("Show EVENT: " + e.toString());
                    break;
                case SWT.Hide:
                    System.out.println("Hide EVENT: " + e.toString());
                    break;
                default:
                    System.out.println(e.toString());
                }
            }
        };
        int[] allEvents = new int[] { SWT.Selection, SWT.Dispose, SWT.Paint,
                SWT.Resize, SWT.MouseDoubleClick, SWT.MouseDown, SWT.MouseUp,
                //SWT.MouseMove,
                SWT.MouseEnter, SWT.MouseExit, SWT.MouseHover, SWT.FocusIn,
                SWT.FocusOut, SWT.KeyDown, SWT.KeyUp, SWT.Traverse, SWT.Show,
                SWT.Hide};
        for (int i = 0; i < allEvents.length; i++) {
            control.addListener(allEvents[i], listener);
        }
        return listener;
    }

    public static void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
        }
    }

    public static void highlight(Control control, int color) {
        //control.setBackground(control.getDisplay().getSystemColor(color));
    }
}