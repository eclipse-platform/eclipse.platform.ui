/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * A base abstract implementation for "copy selection" actions.
 */

public abstract class AbstractCopySelectionAction extends GlobalAction {

	/**
	 * The selection provider.
	 */
	protected ISelectionProvider selectionProvider;

	/**
	 * Sets action's text and tool tip text.
	 * 
	 * @param selectionProvider the selection provider
	 */
	public AbstractCopySelectionAction(ISelectionProvider selectionProvider) {
		super("&Copy"); //$NON-NLS-1$
		this.selectionProvider = selectionProvider;
	}

	/**
	 * Copies the currently selected contents to the clipboard. The meaning of the 
	 * currently selected contents is defined by overriding the getContents() 
	 * method.
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		// puts that content in the clipboard
		Clipboard clipboard = new Clipboard(Display.getCurrent());
		clipboard.setContents(new Object[] {getContents()}, new Transfer[] {TextTransfer.getInstance()});
		clipboard.dispose();
	}

	/**
	 * Registers this action as a global action handler.
	 * 
	 * @param actionBars the action bars where this action will be registered.
	 * @see org.eclipse.core.tools.GlobalAction#registerAsGlobalAction(
	 * org.eclipse.ui.IActionBars)
	 */
	public void registerAsGlobalAction(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, this);
	}

	/**
	 * Returns the currently selected contents as a String object.
	 * 
	 * @return the selected contents as string.
	 */
	protected abstract String getContents();
}