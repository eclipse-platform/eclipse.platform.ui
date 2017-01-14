/*******************************************************************************
 * Copyright (c) 2016 Patrik Suzzi. All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Patrik Suzzi <psuzzi@gmail.com> - initial API and implementation;
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Toggle the visibility of the status bar. Implementation of the
 * {@code org.eclipse.ui.window.togglestatusbar} command.
 *
 */
public class ToggleStatusBarHandler extends AbstractHandler {

	// id of the statusbar, as defined in the LegacyIDE.e4xmi
	private static final String BOTTOM_TRIM_ID = "org.eclipse.ui.trim.status"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		EModelService modelService = window.getService(EModelService.class);
		// get the model element
		MUIElement searchRoot = ((WorkbenchWindow) window).getModel();
		MUIElement trimStatus = modelService.find(BOTTOM_TRIM_ID, searchRoot);
		// toggle statusbar visibility
		if (trimStatus != null) {
			trimStatus.setVisible(!trimStatus.isVisible());
		}
		return null;
	}

}
