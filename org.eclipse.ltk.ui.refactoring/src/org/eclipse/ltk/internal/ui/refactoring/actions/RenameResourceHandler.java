/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.ui.refactoring.actions;

import org.eclipse.osgi.util.NLS;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.RenameResourceWizard;

public class RenameResourceHandler extends AbstractResourcesHandler {
	private static final String LTK_RENAME_COMMAND_NEWNAME_PARAMETER_KEY= "org.eclipse.ltk.ui.refactoring.commands.renameResource.newName.parameter.key"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell activeShell= HandlerUtil.getActiveShell(event);
		Object newNameValue= HandlerUtil.getVariable(event, LTK_RENAME_COMMAND_NEWNAME_PARAMETER_KEY);
		String newName= null;
		if (newNameValue instanceof String) {
			newName= (String) newNameValue;
		} else if (newNameValue != null) {
			RefactoringUIPlugin.logErrorMessage(NLS.bind(RefactoringUIMessages.RenameResourceHandler_ERROR_EXPECTED_STRING, newNameValue.getClass().getName()));
		}
		ISelection sel= HandlerUtil.getCurrentSelection(event);
		if (sel instanceof IStructuredSelection) {
			IResource resource= getCurrentResource((IStructuredSelection) sel);
			if (resource != null) {
				RenameResourceWizard refactoringWizard;
				if (newName != null) {
					refactoringWizard= new RenameResourceWizard(resource, newName);
				} else {
					refactoringWizard= new RenameResourceWizard(resource);
				}
				RefactoringWizardOpenOperation op= new RefactoringWizardOpenOperation(refactoringWizard);
				try {
					op.run(activeShell, RefactoringUIMessages.RenameResourceHandler_title);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
		return null;
	}

	private IResource getCurrentResource(IStructuredSelection sel) {
		IResource[] resources= getSelectedResources(sel);
		if (resources.length == 1) {
			return resources[0];
		}
		return null;
	}
}
