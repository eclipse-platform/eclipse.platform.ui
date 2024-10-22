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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
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
	private static final String LTK_RENAME_COMMAND_NEWNAME_KEY = "org.eclipse.ltk.ui.refactoring.commands.renameResource.newName.parameter.key"; //$NON-NLS-1$
	private static final String LTK_CHECK_COMPOSITE_RENAME_PARAMETER_KEY = "org.eclipse.ltk.ui.refactoring.commands.checkCompositeRename.parameter.key"; //$NON-NLS-1$
	private static final String LTK_COPY_PROJECT_ID = "org.eclipse.ltk.ui.refactoring.commands.copyProject"; //$NON-NLS-1$
	private static final String LTK_COPY_PROJECT_COMMAND_NEWNAME_KEY = "org.eclipse.ltk.ui.refactoring.commands.copyProject.newName.parameter.key"; //$NON-NLS-1$
	private static final String LTK_COPY_PROJECT_COMMAND_NEWLOCATION_KEY = "org.eclipse.ltk.ui.refactoring.commands.copyProject.newLocation.parameter.key"; //$NON-NLS-1$

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
	 * Performs a silent resource rename using the given new name.
	 *
	 * @param newName             The new name to give the resource
	 *
	 * @param structuredSelection The action current selection.
	 *
	 * @return <code>true</code> if we can perform the rename
	 */
	public static boolean renameResource(String newName,
			IStructuredSelection structuredSelection) {
		Map<String, Object> commandParameters = new HashMap<>();
		commandParameters.put(LTK_RENAME_COMMAND_NEWNAME_KEY, newName);
		return runCommand(LTK_RENAME_ID, structuredSelection, commandParameters);
	}

	/**
	 * Open the LTK rename resource wizard if available. The resource's new name
	 * will be inputed in the wizard dialog.
	 *
	 *
	 * @param structuredSelection The action current selection.
	 *
	 * @return <code>true</code> if we can launch the wizard
	 */
	public static boolean openRenameWizard(IStructuredSelection structuredSelection) {
		return runCommand(LTK_RENAME_ID, structuredSelection, Collections.emptyMap());
	}

	/**
	 * Returns true if a rename would result in multiple files being affected
	 * (composite change), false if only the file being renamed is affected.
	 *
	 * @param structuredSelection The action current selection.
	 * @return <code>true</code> if a rename is composite change, <code>false</code>
	 *         otherwise
	 */
	public static boolean isCompositeRename(IStructuredSelection structuredSelection) {
		Map<String, Object> commandParameters = new HashMap<>();
		commandParameters.put(LTK_CHECK_COMPOSITE_RENAME_PARAMETER_KEY, true);
		return runCommand(LTK_RENAME_ID, structuredSelection, commandParameters);
	}

	public static boolean copyProject(IProject project, String newName, IPath newLocation) {
		Map<String, Object> commandParameters = new HashMap<>();
		commandParameters.put(LTK_COPY_PROJECT_COMMAND_NEWNAME_KEY, newName);
		commandParameters.put(LTK_COPY_PROJECT_COMMAND_NEWLOCATION_KEY, newLocation);
		return runCommand(LTK_COPY_PROJECT_ID, new StructuredSelection(project), commandParameters);
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
			Object commandResult;
			if (c != null) {
				commandResult = handlerService.executeCommandInContext(
						new ParameterizedCommand(cmd, null), null, c);
			} else {
				commandResult = handlerService.executeCommand(commandId, null);
			}
			if (commandResult instanceof Boolean) {
				return (Boolean) commandResult;
			}
			return true;
		} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
		}
		return false;
	}
}
