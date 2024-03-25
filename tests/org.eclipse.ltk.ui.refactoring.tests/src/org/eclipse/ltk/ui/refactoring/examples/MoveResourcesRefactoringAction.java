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

import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.MoveResourcesWizard;

public class MoveResourcesRefactoringAction extends Action implements IActionDelegate {

	/*
	 <extension
	     point="org.eclipse.ui.popupMenus">
	  <objectContribution
	        objectClass="org.eclipse.core.resources.IResource"
			adaptable="true"
	        id="org.eclipse.ltk.ui.refactoring.examples.MoveResourcesRefactoringAction">
	     <action
	           label="Move Resources... (ltk.ui.refactoring.examples)"
	           tooltip="Move Resources... (ltk.ui.refactoring.examples)"
	           class="org.eclipse.ltk.ui.refactoring.examples.MoveResourcesRefactoringAction"
	           menubarPath="ExampleRefactoringAction"
	           enablesFor="*"
	           id="MoveResourcesRefactoringAction">
	     </action>
	  </objectContribution>
	</extension>
	 */


	private IResource[] fResources;

	@Override
	public void run(IAction action) {
		Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if (fResources != null && isMoveAvailable(fResources)) {
			try {
				MoveResourcesWizard refactoringWizard= new MoveResourcesWizard(fResources);

				RefactoringWizardOpenOperation op= new RefactoringWizardOpenOperation(refactoringWizard);
				op.run(shell, "Move resources");
			} catch (InterruptedException e) {
				// cancelled
			}
		} else {
			MessageDialog.openInformation(shell, "Move Resources", "Invalid selection. Can only move resources with the same parent.");
		}
	}

	private static boolean isMoveAvailable(IResource[] resources) {
		for (IResource resource : resources) {
			if (!resource.exists()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		fResources= null;
		if (selection instanceof IStructuredSelection) {
			fResources= evaluateResources((IStructuredSelection) selection);
		}
		setEnabled(fResources != null);
	}

	private static IResource[] evaluateResources(IStructuredSelection sel) {
		ArrayList<IResource> res= new ArrayList<>();
		IContainer parent= null;
		for (Object curr : sel.toArray()) {
			if (curr instanceof IFile || curr instanceof IFolder) {
				IResource resource= (IResource) curr;
				if (parent == null) {
					parent= resource.getParent();
				} else if (!parent.equals(resource.getParent())) {
					return null;
				}
				res.add(resource);
			}
		}
		return res.toArray(new IResource[res.size()]);
	}


}
