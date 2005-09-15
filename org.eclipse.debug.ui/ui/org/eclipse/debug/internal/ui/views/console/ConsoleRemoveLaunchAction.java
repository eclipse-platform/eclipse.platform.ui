/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleView;

/**
 * ConsoleRemoveTerminatedAction
 */
public class ConsoleRemoveLaunchAction extends Action implements IDebugEventSetListener, IViewActionDelegate, IConsoleListener {

    private ILaunch fLaunch;

    private IConsoleView fConsoleView;

    public ConsoleRemoveLaunchAction() {
        super(ConsoleMessages.ConsoleRemoveTerminatedAction_0);
        setToolTipText(ConsoleMessages.ConsoleRemoveTerminatedAction_1);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.CONSOLE_REMOVE_LAUNCH);
        setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE));
        setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE));
        setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE));
        DebugPlugin.getDefault().addDebugEventListener(this);
        ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(this);
        update();
    }

    public void dispose() {
        DebugPlugin.getDefault().removeDebugEventListener(this);
        ConsolePlugin.getDefault().getConsoleManager().removeConsoleListener(this);
    }

    public synchronized void update() {
        if (fConsoleView == null) {
            IWorkbenchWindow activeWorkbenchWindow = DebugUIPlugin.getActiveWorkbenchWindow();
            if (activeWorkbenchWindow != null) {
                IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                if (activePage != null) {
                    fConsoleView = (IConsoleView) activePage.findView(IConsoleConstants.ID_CONSOLE_VIEW);
                }
            }
        }

        if (fConsoleView != null) {
            DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
                public void run() {
                    IConsole console = fConsoleView.getConsole();
                    if (console instanceof ProcessConsole) {
                        ProcessConsole processConsole = (ProcessConsole) console;
                        IProcess process = processConsole.getProcess();
                        ILaunch launch = process.getLaunch();
                        if (launch.isTerminated()) {
                            setEnabled(true);
                            fLaunch = launch;
                            return;
                        }
                    }
                }
            });
        }
        fLaunch = null;
        setEnabled(false);
    }

    public synchronized void run() {
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        launchManager.removeLaunch(fLaunch);
    }

    public void handleDebugEvents(DebugEvent[] events) {
        for (int i = 0; i < events.length; i++) {
            DebugEvent event = events[i];
            Object source = event.getSource();
            if (event.getKind() == DebugEvent.TERMINATE && (source instanceof IDebugTarget || source instanceof IProcess)) {
                update();
            }
        }
    }

    public void init(IViewPart view) {
        fConsoleView = (IConsoleView) view;
    }

    public void run(IAction action) {
        run();
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    public void consolesAdded(IConsole[] consoles) {
    }

    public void consolesRemoved(IConsole[] consoles) {
        update();
    }
}
