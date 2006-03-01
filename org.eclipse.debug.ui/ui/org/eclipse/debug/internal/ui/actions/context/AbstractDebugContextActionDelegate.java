/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public abstract class AbstractDebugContextActionDelegate implements IWorkbenchWindowActionDelegate, IActionDelegate2 {

	/**
     *The real action for this delegate 
	 */
	private AbstractDebugContextAction fDebugAction;
    
    /**
     * The underlying action for this delegate
     */
    private IAction fWindowAction;

	public AbstractDebugContextActionDelegate() {
	}

	/*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
        fDebugAction.dispose();

	}

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
     */
    public void init(IAction action) {
        fWindowAction = action;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        setWindow(window);
        fDebugAction.init(window);
	}
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
	public synchronized void run(IAction action) {
        fDebugAction.run();
	}

    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
     */
	public void runWithEvent(IAction action, Event event) {
        run(action);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection s) {
		// do nothing
	}

	protected AbstractDebugContextAction getAction() {
		return fDebugAction;
	}
    
    protected void setAction(AbstractDebugContextAction action) {
        fDebugAction = action;
        action.setDelegate(this);
    }

	protected IWorkbenchWindow getWindow() {
        return fDebugAction.getWindow();
	}

	protected void setWindow(IWorkbenchWindow window) {
		fDebugAction.setWindow(window);
	}

    public void setEnabled(boolean enabled) {
        fWindowAction.setEnabled(enabled);
    }
    
    
}
