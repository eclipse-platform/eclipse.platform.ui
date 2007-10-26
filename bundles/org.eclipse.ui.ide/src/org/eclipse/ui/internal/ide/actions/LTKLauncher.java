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

package org.eclipse.ui.internal.ide.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Launch the LTK aware resource operations ... but sneaky!
 * 
 * @since 3.4
 */
public class LTKLauncher {
	private static final String LTK_DELETE_ID = "org.eclipse.ltk.ui.refactoring.commands.deleteResources"; //$NON-NLS-1$
	private static final String LTK_MOVE_ID = "org.eclipse.ltk.ui.refactoring.commands.moveResources"; //$NON-NLS-1$
	private static final String LTK_RENAME_ID = "org.eclipse.ltk.ui.refactoring.commands.renameResource"; //$NON-NLS-1$

	/**
	 * Open the LTK delete resources wizard if available.
	 * 
	 * @return <code>true</code> if we can launch the wizard
	 */
	public static boolean openDeleteWizard() {
		return runCommand(LTK_DELETE_ID);
	}

	/**
	 * Open the LTK move resources wizard if available.
	 * 
	 * @return <code>true</code> if we can launch the wizard
	 */
	public static boolean openMoveWizard() {
		return runCommand(LTK_MOVE_ID);
	}

	/**
	 * Open the LTK rename resource wizard if available.
	 * 
	 * @return <code>true</code> if we can launch the wizard
	 */
	public static boolean openRenameWizard() {
		return runCommand(LTK_RENAME_ID);
	}

	private static boolean runCommand(String commandId) {
		ICommandService commandService = (ICommandService) PlatformUI
				.getWorkbench().getService(ICommandService.class);
		Command cmd = commandService.getCommand(commandId);
		if (!cmd.isDefined()) {
			return false;
		}

		IHandlerService handlerService = (IHandlerService) PlatformUI
				.getWorkbench().getService(IHandlerService.class);
		try {
			handlerService.executeCommand(commandId, null);
			return true;
		} catch (ExecutionException e) {
		} catch (NotDefinedException e) {
		} catch (NotEnabledException e) {
		} catch (NotHandledException e) {
		}
		return false;
	}
}
