/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
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

	private static boolean isMoveAvailable(IResource[] resource) {
		for (int i= 0; i < resource.length; i++) {
			if (!resource[i].exists())
				return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fResources= null;
		if (selection instanceof IStructuredSelection) {
			fResources= evaluateResources((IStructuredSelection) selection);
		}
		setEnabled(fResources != null);
	}

	private static IResource[] evaluateResources(IStructuredSelection sel) {
		Object[] objects= sel.toArray();
		ArrayList res= new ArrayList();
		IContainer parent= null;
		for (int i= 0; i < objects.length; i++) {
			Object curr= objects[i];
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
		return (IResource[]) res.toArray(new IResource[res.size()]);
	}
	

}
