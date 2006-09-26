/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerService;

public class QuickAccessMenu extends Action implements
		ActionFactory.IWorkbenchAction {
	private IWorkbenchWindow workbenchWindow;

	public QuickAccessMenu(IWorkbenchWindow window) {
		super(WorkbenchMessages.QuickAccessAction_text);
		workbenchWindow = window;
        setToolTipText(WorkbenchMessages.QuickAccessAction_toolTip); 
		setActionDefinitionId("org.eclipse.ui.window.quickAccess"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		if (workbenchWindow == null) {
			return;
		}

		IHandlerService handlerService = (IHandlerService) workbenchWindow
				.getService(IHandlerService.class);
		try {
			handlerService.executeCommand(getActionDefinitionId(),
					null);
		} catch (Exception e) {
			WorkbenchPlugin.log(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
	 */
	public void dispose() {
		workbenchWindow = null;
	}
}
