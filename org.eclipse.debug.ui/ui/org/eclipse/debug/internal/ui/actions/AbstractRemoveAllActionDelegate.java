/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

 
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This class is a base implementation of a 'remove all' debug action
 * 
 * This class is intended to be extended by clients
 * @see IViewActionDelegate
 * @see IActionDelegate2
 * @see IWorkbenchWindowActionDelegate
 */
public abstract class AbstractRemoveAllActionDelegate implements IViewActionDelegate, IActionDelegate2, IWorkbenchWindowActionDelegate {
	
	/**
	 * The underlying <code>IAction</code>
	 */
	private IAction fAction;

	/**
	 * Needed for reflective creation
	 */
	public AbstractRemoveAllActionDelegate() {}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		fAction = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		fAction = action;
	}

	/**
	 * Returns this delegate's action.
	 * 
	 * @return the underlying <code>IAction</code>
	 */
	protected IAction getAction() {
		return fAction;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		initialize();
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		initialize();
		update();
	}

	/**
	 * Initializes any listeners, etc.
	 */
	protected abstract void initialize();
	
	/**
	 * Update enablement.
	 */
	protected void update() {
		IAction action = getAction();
		if (action != null) {
			action.setEnabled(isEnabled());
		}
	}
	
	/**
	 * Returns whether this action is enabled
	 * 
	 * @return true if this action is enabled, false otherwise
	 */
	protected abstract boolean isEnabled();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection s) {
		// do nothing
	}
}
