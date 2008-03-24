/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.PerspectiveMenu;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Change the perspective of the active page in the window
 * to the selected one.
 */
public class ChangeToPerspectiveMenu extends PerspectiveMenu {

    /**
     * Constructor for ChangeToPerspectiveMenu.
     * 
     * @param window the workbench window this action applies to.
     */
    public ChangeToPerspectiveMenu(IWorkbenchWindow window, String id) {
        super(window, id);
        // indicate that a open perspectives submenu has been created
        ((WorkbenchWindow) window)
                .addSubmenu(WorkbenchWindow.OPEN_PERSPECTIVE_SUBMENU);
        showActive(true);
    }

    /* (non-Javadoc)
     * @see PerspectiveMenu#run(IPerspectiveDescriptor)
     */
    protected void run(IPerspectiveDescriptor desc) {
		IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
		int mode = store.getInt(IPreferenceConstants.OPEN_PERSP_MODE);
		IWorkbenchPage page = getWindow().getActivePage();
		IPerspectiveDescriptor persp = null;
		if (page != null) {
			persp = page.getPerspective();
		}

		IHandlerService handlerService = (IHandlerService) getWindow()
				.getService(IHandlerService.class);
		ICommandService commandService = (ICommandService) getWindow()
				.getService(ICommandService.class);

		Command command = commandService
				.getCommand(SHOW_PERSP_ID);
		Map parameters = new HashMap();
		parameters
				.put(
						"org.eclipse.ui.perspectives.showPerspective.perspectiveId", desc.getId()); //$NON-NLS-1$

		// Only open a new window if user preference is set and the window
		// has an active perspective.
		if (IPreferenceConstants.OPM_NEW_WINDOW == mode && persp != null) {

			//Call the handler!
			//Set up the param for newWindow!
			parameters
					.put(
							"org.eclipse.ui.perspectives.showPerspective.newWindow", "true"); //$NON-NLS-1$//$NON-NLS-2$
		}
		
		ParameterizedCommand pCommand = ParameterizedCommand.generateCommand(
				command, parameters);
		try {
			handlerService.executeCommand(pCommand, null);
		} catch (ExecutionException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.WARNING, WorkbenchPlugin.PI_WORKBENCH,
							"Failed to execute " + SHOW_PERSP_ID, e)); //$NON-NLS-1$
		} catch (NotDefinedException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.WARNING, WorkbenchPlugin.PI_WORKBENCH,
							"Failed to execute " + SHOW_PERSP_ID, e)); //$NON-NLS-1$
		} catch (NotEnabledException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.WARNING, WorkbenchPlugin.PI_WORKBENCH,
							"Failed to execute " + SHOW_PERSP_ID, e)); //$NON-NLS-1$
		} catch (NotHandledException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.WARNING, WorkbenchPlugin.PI_WORKBENCH,
							"Failed to execute " + SHOW_PERSP_ID, e)); //$NON-NLS-1$
		}
	}
}
