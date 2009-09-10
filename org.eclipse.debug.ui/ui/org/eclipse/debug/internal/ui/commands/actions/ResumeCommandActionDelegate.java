/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.ui.actions.DebugCommandAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Resume action delegate.
 * 
 * @since 3.3
 */
public class ResumeCommandActionDelegate implements IWorkbenchWindowActionDelegate, IActionDelegate2 {

    private DebugCommandAction fDebugAction = new ResumeCommandAction();
    
    public void dispose() {
        fDebugAction.dispose();
    }

    public void init(IWorkbenchWindow window) {
        fDebugAction.init(window);
    }

    public void run(IAction action) {
        fDebugAction.run();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

    public void init(IAction action) {
        fDebugAction.setActionProxy(action);
        
    }

    public void runWithEvent(IAction action, Event event) {
        run(action);
    }
}
