/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.intro.impl.IntroPlugin;

public class Util {

    /**
     * Handle the exception by logging to the Log. <br>
     * The errorId is used to NL enable the error message. Pass
     * <code>null</code> as the messageId to indicate that the error's message
     * should be shown as the primary message
     */
    public static void handleException(String errorId, Exception e) {
        handleException(errorId, e, null);
    }

    /**
     * Handle the exception by logging to the Log. <br>
     * The errorId is used to NL enable the error message, and the variables are
     * subsituted in the message. Pass <code>null</code> as the messageId to
     * indicate that the error's message should be shown as the primary message
     */
    public static void handleException(String errorId, Exception e,
            Object[] variables) {
        String msg = null;
        if (variables != null) {
            // if variables is not null, errorId will never be null.
            msg = IntroPlugin.getFormattedString(errorId, variables);
        } else {
            if (errorId == null)
                msg = e.getMessage();
            else
                msg = IntroPlugin.getString(errorId);
        }
        Log.error(msg, e);
    }

    /**
     * Handle the exception by displaying an Error Dialog. <br>
     * The errorId is used to NL enable the error message. Also, the error is
     * logged by the Log. Pass <code>null</code> as the messageId to indicate
     * that the error's message should be shown as the primary message
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
                    System.out.println("Selection EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.Dispose:
                    System.out.println("Dispose EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.Paint:
                    System.out.println("Paint EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.Resize:
                    System.out.println("Resize EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.MouseDoubleClick:
                    System.out.println("MouseDoubleClick EVENT: " //$NON-NLS-1$
                            + e.toString());
                    break;
                case SWT.MouseDown:
                    System.out.println("MouseDown EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.MouseUp:
                    System.out.println("MouseUp EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.MouseMove:
                    System.out.println("MouseMove EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.MouseEnter:
                    System.out.println("MouseEnter EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.MouseExit:
                    System.out.println("MouseExit EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.MouseHover:
                    System.out.println("MouseHover EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.FocusIn:
                    System.out.println("FocusIn EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.FocusOut:
                    System.out.println("FocusOut EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.KeyDown:
                    System.out.println("KeyDown EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.KeyUp:
                    System.out.println("KeyUp EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.Traverse:
                    System.out.println("Traverse EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.Show:
                    System.out.println("Show EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                case SWT.Hide:
                    System.out.println("Hide EVENT: " + e.toString()); //$NON-NLS-1$
                    break;
                default:
                    System.out.println(e.toString());
                }
            }
        };
        int[] allEvents = new int[] { SWT.Selection, SWT.Dispose, SWT.Paint,
                SWT.Resize, SWT.MouseDoubleClick, SWT.MouseDown, SWT.MouseUp,
                // SWT.MouseMove,
                SWT.MouseEnter, SWT.MouseExit, SWT.MouseHover, SWT.FocusIn,
                SWT.FocusOut, SWT.KeyDown, SWT.KeyUp, SWT.Traverse, SWT.Show,
                SWT.Hide };
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
        control.setBackground(control.getDisplay().getSystemColor(color));
    }

    public static void highlightFocusControl() {
        Control control = Display.getCurrent().getFocusControl();
        if (control != null)
            control.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_DARK_RED));
    }

    /**
     * Launch an external brwoser on the given url.
     */
    public static boolean openBrowser(String href) {
        try {
            URL url = new URL(href);
            IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
                .getBrowserSupport();
            support.getExternalBrowser().openURL(url);
            return true;
        } catch (PartInitException e) {
            Log.error("Intro failed to get Browser support.", e);
            return false;
        } catch (MalformedURLException e) {
            Log.error("Intro failed to display: " + href, e);
            return false;
        }
    }

    /**
     * Display an error message if opening an external browser failes.
     */
    private static void openBrowserError(final Display display,
            final Exception e) {
        display.asyncExec(new Runnable() {

            public void run() {
                DialogUtil.displayErrorMessage(display.getActiveShell(),
                    IntroPlugin.getString("OpenBroswer.failedToLaunch"), e); //$NON-NLS-1$
            }
        });
    }


}
