/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.intro.impl.util;

import java.io.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.*;
import org.eclipse.swt.program.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.intro.impl.*;

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
                //SWT.MouseMove,
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
        // format the href for an html file (file:///<filename.html>
        // required for Mac only.
        if (href.startsWith("file:")) { //$NON-NLS-1$
            href = href.substring(5);
            while (href.startsWith("/")) { //$NON-NLS-1$
                href = href.substring(1);
            }
            href = "file:///" + href; //$NON-NLS-1$
        }
        final String localHref = href;

        final Display display = Display.getCurrent();
        String platform = SWT.getPlatform();

        if ("win32".equals(platform)) { //$NON-NLS-1$
            return Program.launch(localHref);
        } else if ("carbon".equals(platform)) { //$NON-NLS-1$
            try {
                Process process = Runtime.getRuntime().exec(
                        "/usr/bin/open " + localHref); //$NON-NLS-1$
                if (process == null)
                    return false;
                return process.exitValue() == 0 ? true : false;
            } catch (IOException e) {
                openBrowserError(display, e);
                return false;
            }
        } else {
            final boolean[] result = new boolean[1];
            Thread launcher = new Thread("Intro browser Launcher") {//$NON-NLS-1$

                public void run() {
                    try {
                        Process process = doOpenBrowser(localHref, true);
                        if (process == null)
                            // no browser already opened. Launch new one.
                            process = doOpenBrowser(localHref, false);
                        if (process == null)
                            result[0] = false;
                        result[0] = process.exitValue() == 0 ? true : false;

                    } catch (Exception e) {
                        openBrowserError(display, e);
                        result[0] = false;
                    }
                }

                private Process doOpenBrowser(String href, boolean remote)
                        throws Exception {
                    Process p = null;
                    String webBrowser;
                    // try netscape first.
                    webBrowser = "netscape"; //$NON-NLS-1$
                    String cmd = createCommand(webBrowser, href, remote);
                    try {
                        p = Runtime.getRuntime().exec(cmd);
                    } catch (IOException e) {
                        // command failed
                        p = null;
                    }
                    if (p != null) {
                        int exitCode = p.waitFor();
                        if (exitCode == 0)
                            return p;
                    }

                    // netscape failed. Try mozilla.
                    webBrowser = "mozilla"; //$NON-NLS-1$
                    cmd = createCommand(webBrowser, href, remote);
                    try {
                        p = Runtime.getRuntime().exec(cmd);
                    } catch (IOException e) {
                        // command failed
                        p = null;
                    }
                    if (p != null) {
                        int exitCode = p.waitFor();
                        if (exitCode == 0)
                            return p;
                    }

                    // all failed. return null
                    return null;
                }

                /**
                 * Create a command to launch the given browser, with/without
                 * remote control.
                 *  
                 */
                private String createCommand(String browser, String href,
                        boolean remote) {
                    StringBuffer cmd = new StringBuffer(browser);
                    if (remote) {
                        cmd.append(" -remote openURL("); //$NON-NLS-1$
                        cmd.append(href);
                        cmd.append(")"); //$NON-NLS-1$
                    } else {
                        cmd.append(" "); //$NON-NLS-1$
                        cmd.append(href);
                    }
                    return cmd.toString();
                }
            };
            launcher.start();
            return result[0];
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