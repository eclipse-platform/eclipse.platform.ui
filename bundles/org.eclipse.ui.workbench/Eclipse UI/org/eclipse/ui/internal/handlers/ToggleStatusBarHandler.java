/*******************************************************************************
 * Copyright (c) 2017 Patrik Suzzi. All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Patrik Suzzi <psuzzi@gmail.com> - initial API and implementation;
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceScopes;

/**
 * Toggle the visibility of the status bar. Implementation of the
 * {@code org.eclipse.ui.window.togglestatusbar} command.
 *
 */
public class ToggleStatusBarHandler extends AbstractHandler implements IElementUpdater {

	public static final String COMMAND_ID_TOGGLE_STATUSBAR = "org.eclipse.ui.window.togglestatusbar"; //$NON-NLS-1$

	// id of the statusbar, as defined in the LegacyIDE.e4xmi
	private static final String BOTTOM_TRIM_ID = "org.eclipse.ui.trim.status"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (!(window instanceof WorkbenchWindow))
			return null;
		MUIElement trimStatus = getTrimStatus((WorkbenchWindow) window);

		if (trimStatus != null) {
			// toggle statusbar visibility
			trimStatus.setVisible(!trimStatus.isVisible());
			// refresh menu item label, triggering updateElement()
			ICommandService commandService = window.getService(ICommandService.class);
			Map<String, WorkbenchWindow> filter = new HashMap<>();
			filter.put(IServiceScopes.WINDOW_SCOPE, (WorkbenchWindow) window);
			commandService.refreshElements(COMMAND_ID_TOGGLE_STATUSBAR, filter);
		}
		return null;
	}

	/**
	 * Updates the visibilty status of the element.
	 */
	@Override
	public void updateElement(UIElement element, Map parameters) {
		IWorkbenchLocationService wls = element
				.getServiceLocator()
				.getService(IWorkbenchLocationService.class);
		IWorkbenchWindow window = wls.getWorkbenchWindow();
		if (!(window instanceof WorkbenchWindow))
			return;
		MUIElement trimStatus = getTrimStatus((WorkbenchWindow) window);
		if(trimStatus != null) {
			element.setText(trimStatus.isVisible() ? WorkbenchMessages.ToggleStatusBarVisibilityAction_hide_text
					: WorkbenchMessages.ToggleStatusBarVisibilityAction_show_text);
		}
	}

	/* Get the MUIElement representing the status bar */
	private static MUIElement getTrimStatus(WorkbenchWindow window) {
		EModelService modelService = window.getService(EModelService.class);
		MUIElement searchRoot = window.getModel();
		return modelService.find(BOTTOM_TRIM_ID, searchRoot);
	}

}
