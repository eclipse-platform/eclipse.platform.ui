/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.ui.internal.dialogs.WorkbenchEditorsDialog;

/**
 * Opens a dialog showing all open editors and the recently closed editors.
 *
 * @since 3.4
 *
 */
public class WorkbenchEditorsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		if (workbenchWindow == null) {
			// action has been disposed
			return null;
		}
		IWorkbenchPage page = workbenchWindow.getActivePage();
		if (page != null) {
			new WorkbenchEditorsDialog(workbenchWindow).open();
		}
		return null;
	}

}
