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
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.examples;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.DeleteResourcesWizard;

public class DeleteResourcesRefactoringAction extends Action implements IActionDelegate {

	/*
	 <extension
	     point="org.eclipse.ui.popupMenus">
	  <objectContribution
	        objectClass="org.eclipse.core.resources.IResource"
			adaptable="true"
	        id="org.eclipse.ltk.ui.refactoring.examples.DeleteResourcesRefactoringAction">
	     <action
	           label="Delete Resources... (ltk.ui.refactoring.examples)"
	           tooltip="Delete Resources... (ltk.ui.refactoring.examples)"
	           class="org.eclipse.ltk.ui.refactoring.examples.DeleteResourcesRefactoringAction"
	           menubarPath="ExampleRefactoringAction"
	           enablesFor="*"
	           id="DeleteResourcesRefactoringAction">
	     </action>
	  </objectContribution>

	</extension>
	 */


	private IResource[] fResources;

	@Override
	public void run(IAction action) {
		Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if (fResources != null && isDeleteAvailable(fResources)) {
			try {
				DeleteResourcesWizard refactoringWizard= new DeleteResourcesWizard(fResources);

				RefactoringWizardOpenOperation op= new RefactoringWizardOpenOperation(refactoringWizard);
				op.run(shell, "Delete resources");
			} catch (InterruptedException e) {
				// cancelled
			}
		} else {
			MessageDialog.openInformation(shell, "Delete Resources", "Invalid selection. Can not mix projects with files and folders. Files and folders must not be nested or nesting.");
		}
	}

	private static boolean isDeleteAvailable(IResource[] resources) {
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
		for (Object curr : sel.toArray()) {
			if (curr instanceof IFile || curr instanceof IFolder) {
				if (!addFileOrFolder(res, (IResource) curr)) {
					return null;
				}
			} else if (curr instanceof IProject) {
				if (!addProject(res, (IProject) curr)) {
					return null;
				}
			} else {
				return null;
			}
		}
		return res.toArray(new IResource[res.size()]);
	}

	private static boolean addProject(ArrayList<IResource> res, IProject project) {
		if (!res.isEmpty()) {
			if (!(res.get(0) instanceof IProject)) { // either all projects or all IFile/IFolder
				return false;
			}
			// not yet in list
			if (res.contains(project)) {
				return false;
			}
		}
		res.add(project);
		return true;
	}

	private static boolean addFileOrFolder(ArrayList<IResource> res, IResource resource) {
		if (!res.isEmpty()) {
			if (res.get(0) instanceof IProject) { // either all projects or all IFile/IFolder
				return false;
			}

			// not in list and no nested or nesting element in list
			IPath path= resource.getFullPath();
			for (IResource curr : res) {
				IPath currPath= curr.getFullPath();
				if (path.isPrefixOf(currPath) || currPath.isPrefixOf(path) || currPath.equals(path)) {
					return false;
				}
			}
		}
		res.add(resource);
		return true;
	}


}
