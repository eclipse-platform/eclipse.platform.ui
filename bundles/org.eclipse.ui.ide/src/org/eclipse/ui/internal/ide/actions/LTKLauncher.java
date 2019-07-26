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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *******************************************************************************/

package org.eclipse.ui.internal.ide.actions;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
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
	private static final String LTK_RENAME_COMMAND_NEWNAME_PARAMETER_KEY = "org.eclipse.ltk.ui.refactoring.commands.renameResource.newName.parameter.key"; //$NON-NLS-1$
	/**
	 * Open the LTK delete resources wizard if available.
	 *
	 * @param structuredSelection
	 *            The action current selection.
	 *
	 * @return <code>true</code> if we can launch the wizard
	 */
	public static boolean openDeleteWizard(
			IStructuredSelection structuredSelection) {
		return runCommand(LTK_DELETE_ID, structuredSelection, Collections.emptyMap());
	}

	/**
	 * Open the LTK move resources wizard if available.
	 *
	 * @param structuredSelection
	 *            The action current selection.
	 *
	 * @return <code>true</code> if we can launch the wizard
	 */
	public static boolean openMoveWizard(
			IStructuredSelection structuredSelection) {
		return runCommand(LTK_MOVE_ID, structuredSelection, Collections.emptyMap());
	}

	/**
	 * Open the LTK rename resource wizard if available.
	 *
	 * @param newName             The new name to give the resource. If null is
	 *                            given, then the LTK rename resource wizard will
	 *                            prompt the user for a new name.
	 *
	 * @param structuredSelection The action current selection.
	 *
	 * @return <code>true</code> if we can launch the wizard
	 */
	public static boolean openRenameWizard(String newName,
			IStructuredSelection structuredSelection) {
		Map<String, Object> commandParameters;
		if (newName != null) {
			commandParameters = Collections.singletonMap(LTK_RENAME_COMMAND_NEWNAME_PARAMETER_KEY, newName);
		} else {
			commandParameters = Collections.emptyMap();
		}
		return runCommand(LTK_RENAME_ID, structuredSelection, commandParameters);
	}

	private static boolean runCommand(String commandId, IStructuredSelection selection,
			Map<String, Object> commandParameters) {

		ICommandService commandService = PlatformUI
				.getWorkbench().getService(ICommandService.class);
		Command cmd = commandService.getCommand(commandId);
		if (!cmd.isDefined()) {
			return false;
		}

		IHandlerService handlerService = PlatformUI
				.getWorkbench().getService(IHandlerService.class);
		EvaluationContext c = null;
		if (selection != null) {
			c = new EvaluationContext(handlerService
					.createContextSnapshot(false), selection.toList());
			c.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
			for (Entry<String, Object> entry : commandParameters.entrySet()) {
				c.addVariable(entry.getKey(), entry.getValue());
			}
		}
		try {
			if (c != null) {
				handlerService.executeCommandInContext(
						new ParameterizedCommand(cmd, null), null, c);
			} else {
				handlerService.executeCommand(commandId, null);
			}
			return true;
		} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
		}
		return false;
	}
}
