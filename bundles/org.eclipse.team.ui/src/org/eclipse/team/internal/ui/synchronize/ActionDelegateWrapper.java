/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.*;

/**
 * An Action that wraps IActionDelegates so they can be used programatically
 * in toolbars, etc.
 */
public class ActionDelegateWrapper extends Action {
	
	private IActionDelegate delegate;

	public ActionDelegateWrapper(IActionDelegate delegate, IWorkbenchPart part) {
		this.delegate = delegate;
		if(part != null) {
			if (delegate instanceof IObjectActionDelegate) {
				((IObjectActionDelegate)delegate).setActivePart(this, part);
			}
			if (part instanceof IViewPart 
					&& delegate instanceof IViewActionDelegate) {
				((IViewActionDelegate)delegate).init((IViewPart)part);
			}
			if (part instanceof IEditorPart 
					&& delegate instanceof IViewActionDelegate) {
				((IEditorActionDelegate)delegate).setActiveEditor(this, (IEditorPart)part);
			}
		}
		// Assume there is no selection untiul told otherwise
		setSelection(StructuredSelection.EMPTY);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		delegate.run(this);
	}

	/**
	 * Return the delegate associated with this action.
	 * @return the delegate associated with this action
	 */
	public IActionDelegate getDelegate() {
		return delegate;
	}

	/**
	 * Set the selection of the action 
	 * @param selection the selection
	 */
	public void setSelection(ISelection selection) {
		getDelegate().selectionChanged(this, selection);
	}
	
	/**
	 * Set the selection of the action to the given object
	 * @param input the selected object
	 */
	public void setSelection(Object input) {
		ISelection selection = new StructuredSelection(input);
		setSelection(selection);
	}
}
