/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
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
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.actions;


import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IProject;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CopyRefactoring;
import org.eclipse.ltk.internal.core.refactoring.resource.CopyProjectProcessor;

public class CopyProjectHandler extends AbstractResourcesHandler {

	private static final String LTK_COPY_PROJECT_COMMAND_NEWNAME_KEY = "org.eclipse.ltk.ui.refactoring.commands.copyProject.newName.parameter.key"; //$NON-NLS-1$
	private static final String LTK_COPY_PROJECT_COMMAND_NEWLOCATION_KEY = "org.eclipse.ltk.ui.refactoring.commands.copyProject.newLocation.parameter.key"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {


		Object newNameValue= HandlerUtil.getVariable(event, LTK_COPY_PROJECT_COMMAND_NEWNAME_KEY);
		Object newLocationValue= HandlerUtil.getVariable(event, LTK_COPY_PROJECT_COMMAND_NEWLOCATION_KEY);
		ISelection sel= HandlerUtil.getCurrentSelection(event);

		String newName= null;
		if (newNameValue instanceof String) {
			newName= (String) newNameValue;
		}

		IPath newLocation= null;
		if (newLocationValue instanceof IPath) {
			newLocation= (IPath) newLocationValue;
		}

		if (sel instanceof IStructuredSelection selection) {
			List<IProject> resources= Arrays.stream(getSelectedResources(selection))
					.filter(IProject.class::isInstance)
					.map(IProject.class::cast)
					.toList();
			if (resources.size() == 1) {

				CopyRefactoring copyRefactoring= new CopyRefactoring(new CopyProjectProcessor(resources.get(0), newName, newLocation));
				try {
					CreateChangeOperation create= new CreateChangeOperation(
							new CheckConditionsOperation(copyRefactoring, CheckConditionsOperation.FINAL_CONDITIONS),
							RefactoringStatus.FATAL);

					PerformChangeOperation perform= new PerformChangeOperation(create);
					perform.setUndoManager(RefactoringCore.getUndoManager(), copyRefactoring.getName());

					perform.run(new NullProgressMonitor());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
}
