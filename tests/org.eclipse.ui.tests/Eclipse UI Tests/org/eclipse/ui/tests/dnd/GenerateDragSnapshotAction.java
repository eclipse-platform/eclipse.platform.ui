/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dnd;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.tests.TestPlugin;

/**
 * @since 3.0
 */
public class GenerateDragSnapshotAction implements
        IWorkbenchWindowActionDelegate {

    IWorkbenchWindow window;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        FileDialog dialog = new FileDialog(window.getShell());

        dialog.setFileName("dragtests.xml");

        final String filename = dialog.open();

        if (filename == null) {
            return;
        }

        final DragTestSuite testSuite = new DragTestSuite();

        try {
            new ProgressMonitorDialog(window.getShell()).run(true, true,
                    new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException,
                                InterruptedException {
                            try {
                                final IProgressMonitor finalMon = monitor;

                                Display.getDefault().syncExec(new Runnable() {
                                    public void run() {
                                        Map result;
                                        try {
                                            result = testSuite
                                                    .generateExpectedResults(finalMon);
                                            if (!finalMon.isCanceled()) {
                                                DragTestSuite.saveResults(
                                                        filename, result);
                                            }
                                        } catch (Exception e) {
                                            openError(e);
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                openError(e);
                            }
                        }
                    });
        } catch (Exception e) {
            openError(e);
        }
    }

    private void openError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            msg = e.getClass().getName();
        }

        e.printStackTrace();

        IStatus status = new Status(Status.ERROR, TestPlugin.getDefault()
                .getDescriptor().getUniqueIdentifier(), 0, msg, e);

        TestPlugin.getDefault().getLog().log(status);

        ErrorDialog.openError(window.getShell(), "Error", msg, status);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }
}