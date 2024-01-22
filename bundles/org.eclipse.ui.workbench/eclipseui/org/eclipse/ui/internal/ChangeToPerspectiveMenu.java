/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.PerspectiveMenu;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Change the perspective of the active page in the window to the selected one.
 */
public class ChangeToPerspectiveMenu extends PerspectiveMenu {

	/**
	 * Constructor for ChangeToPerspectiveMenu.
	 *
	 * @param window the workbench window this action applies to
	 * @param id     the menu id
	 */
	public ChangeToPerspectiveMenu(IWorkbenchWindow window, String id) {
		super(window, id);
		// indicate that a open perspectives submenu has been created
		if (window instanceof WorkbenchWindow) {
			((WorkbenchWindow) window).addSubmenu(WorkbenchWindow.OPEN_PERSPECTIVE_SUBMENU);
		}
		showActive(true);
	}

	@Override
	protected void run(IPerspectiveDescriptor desc) {
		IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
		int mode = store.getInt(IPreferenceConstants.OPEN_PERSP_MODE);
		IWorkbenchPage page = getWindow().getActivePage();
		IPerspectiveDescriptor persp = null;
		if (page != null) {
			persp = page.getPerspective();
		}

		IHandlerService handlerService = getWindow().getService(IHandlerService.class);
		ICommandService commandService = getWindow().getService(ICommandService.class);

		Command command = commandService.getCommand(IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE);
		Map parameters = new HashMap();
		parameters.put(IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE_PARM_ID, desc.getId());

		// Only open a new window if user preference is set and the window
		// has an active perspective.
		if (IPreferenceConstants.OPM_NEW_WINDOW == mode && persp != null) {

			// Call the handler!
			// Set up the param for newWindow!
			parameters.put("org.eclipse.ui.perspectives.showPerspective.newWindow", "true"); //$NON-NLS-1$//$NON-NLS-2$
		}

		ParameterizedCommand pCommand = ParameterizedCommand.generateCommand(command, parameters);
		try {
			handlerService.executeCommand(pCommand, null);
		} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
			StatusManager.getManager().handle(new Status(IStatus.WARNING, WorkbenchPlugin.PI_WORKBENCH,
					"Failed to execute " + IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE, e)); //$NON-NLS-1$
		}
	}
}
