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

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;

public class SelectAllAction extends GlobalAction {

	private ITextOperationTarget target;

	public SelectAllAction(ITextOperationTarget target) {
		super("Select all"); //$NON-NLS-1$
		this.target = target;
	}

	/**
	 * Registers this action as a global action handler.
	 * 
	 * @param actionBars the action bars where this action will be registered.
	 * @see org.eclipse.core.tools.GlobalAction#registerAsGlobalAction(org.eclipse.ui.IActionBars)
	 */
	public void registerAsGlobalAction(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.SELECT_ALL, this);
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		target.doOperation(ITextOperationTarget.SELECT_ALL);
	}

}