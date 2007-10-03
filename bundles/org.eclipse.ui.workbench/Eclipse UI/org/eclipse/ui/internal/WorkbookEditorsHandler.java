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

package org.eclipse.ui.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Shows a list of open editors in the current or last active workbook.
 * 
 * @since 3.4
 * 
 */
public class WorkbookEditorsHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow workbenchWindow = HandlerUtil
				.getActiveWorkbenchWindow(event);
		if (workbenchWindow == null) {
			// action has been disposed
			return null;
		}
		IWorkbenchPage page = workbenchWindow.getActivePage();
		if (page != null) {
			WorkbenchPage wbp = (WorkbenchPage) page;
			EditorAreaHelper eah = wbp.getEditorPresentation();
			if (eah != null) {
				eah.displayEditorList();
			}
		}
		return null;
	}

}
