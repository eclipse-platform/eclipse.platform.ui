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
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.examples;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.RenameResourceWizard;

public class RenameResourceRefactoringAction extends Action implements IActionDelegate {

	/*
	 <extension
	     point="org.eclipse.ui.popupMenus">
	  <objectContribution
	        objectClass="org.eclipse.core.resources.IResource"
			adaptable="true"
	        id="org.eclipse.ltk.ui.refactoring.examples.RenameResourceRefactoringAction">
	     <action
	           label="Rename Resource... (ltk.ui.refactoring.examples)"
	           tooltip="Rename Resource... (ltk.ui.refactoring.examples)"
	           class="org.eclipse.ltk.ui.refactoring.examples.RenameResourceRefactoringAction"
	           menubarPath="ExampleRefactoringAction"
	           enablesFor="1"
	           id="RenameResourceRefactoringAction">
	     </action>
	  </objectContribution>
	</extension>
	 */


	private IResource fResource;

	@Override
	public void run(IAction action) {
		if (fResource != null && isRenameAvailable(fResource)) {
			Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			try {
				RenameResourceWizard refactoringWizard= new RenameResourceWizard(fResource);

				RefactoringWizardOpenOperation op= new RefactoringWizardOpenOperation(refactoringWizard);
				op.run(shell, "Rename resource");
			} catch (InterruptedException e) {
				// cancelled
			}
		}
	}

	private static boolean isRenameAvailable(final IResource resource) {
		if (resource == null)
			return false;
		if (!resource.exists())
			return false;
		if (!resource.isAccessible())
			return false;
		return true;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		fResource= null;
		if (selection instanceof IStructuredSelection) {
			Object object= ((IStructuredSelection) selection).getFirstElement();
			if (object instanceof IResource) {
				fResource= (IResource) object;
			}
		}
	}

}
