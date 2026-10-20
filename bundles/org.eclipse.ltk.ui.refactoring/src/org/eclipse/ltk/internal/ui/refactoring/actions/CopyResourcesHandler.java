/*******************************************************************************
 * Copyright (c) 2024, 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial implementation
 *     Felix Schmid - adapted for copy resource handler
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.actions;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CopyRefactoring;
import org.eclipse.ltk.internal.core.refactoring.resource.CopyResourcesProcessor;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.ui.refactoring.RefactoringUI;

public class CopyResourcesHandler extends AbstractResourcesHandler {

	private static final String LTK_COPY_RESOURCE_COMMAND_DESTINATION_KEY = "org.eclipse.ltk.ui.refactoring.commands.copyResources.destinationPaths.parameter.key"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel= HandlerUtil.getCurrentSelection(event);
		Object dest= HandlerUtil.getVariable(event, LTK_COPY_RESOURCE_COMMAND_DESTINATION_KEY);
		Shell shell= HandlerUtil.getActiveShell(event);

		if (sel instanceof IStructuredSelection selection && dest instanceof IPath[] destPaths) {
			IResource[] resources= getSelectedResources(selection);
			CopyRefactoring copyRefactoring= new CopyRefactoring(new CopyResourcesProcessor(resources, destPaths));
			try {
				CreateChangeOperation create= new CreateChangeOperation(
						new CheckConditionsOperation(copyRefactoring, CheckConditionsOperation.ALL_CONDITIONS),
						RefactoringStatus.FATAL);

				PerformChangeOperation perform= new PerformChangeOperation(create);
				perform.setUndoManager(RefactoringCore.getUndoManager(), copyRefactoring.getName());
				perform.run(new NullProgressMonitor());

				if (perform.getConditionCheckingStatus().getSeverity() >= RefactoringStatus.WARNING) {
					openErrorDialog(shell, perform.getConditionCheckingStatus());
				}
				if (perform.getConditionCheckingStatus().getSeverity() >= RefactoringStatus.FATAL) {
					throw new ExecutionException(
							perform.getConditionCheckingStatus().getEntryWithHighestSeverity().getMessage());
				}
			} catch (CoreException e) {
				openErrorDialog(shell, e.getStatus());
				throw new ExecutionException(e.getMessage(), e);
			}
		} else { // sel or dest were of incorrect type
			throw new ExecutionException(RefactoringUIMessages.CopyResourcesHandler_incorrect_params);
		}
		return null;
	}

	private void openErrorDialog(Shell shell, RefactoringStatus status) {
		Display.getDefault().asyncExec(() -> RefactoringUI.createLightWeightStatusDialog(status, shell,
				RefactoringUIMessages.CopyResourcesHandler_problem_occurred).open());
	}

	private void openErrorDialog(Shell shell, IStatus status) {
		Display.getDefault().asyncExec(() -> ErrorDialog.openError(shell, null, null, status));
	}
}
