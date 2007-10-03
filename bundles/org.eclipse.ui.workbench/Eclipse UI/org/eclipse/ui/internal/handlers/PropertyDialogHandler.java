/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.dialogs.PropertyDialog;

/**
 * @since 3.4
 * 
 */
public class PropertyDialogHandler extends AbstractHandler {

	private String initialPageId = null;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PreferenceDialog dialog;
		Object element = null;
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		Shell shell;
		
		if (currentSelection instanceof IStructuredSelection) {
			element = ((IStructuredSelection) currentSelection)
					.getFirstElement();
		} else {
			return null;
		}
		
		if (activeWorkbenchWindow != null){
			shell = activeWorkbenchWindow.getShell();
			dialog = PropertyDialog.createDialogOn(shell,
					initialPageId, element);
			if (dialog != null) {
				dialog.open();
			}
		}
		return null;
	}

}
