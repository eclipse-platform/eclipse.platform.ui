/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.examples;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

/* In plugin.xml:
   <extension
         point="org.eclipse.ui.popupMenus">
      <objectContribution
            objectClass="org.eclipse.core.resources.IFile"
			adaptable="true"
            id="org.eclipse.ltk.ui.refactoring.examples.ExampleRefactoringAction">
         <action
               label="Replace content... (ltk.ui.refactoring.examples)"
               tooltip="Replace content... (ltk.ui.refactoring.examples)"
               class="org.eclipse.ltk.ui.refactoring.examples.ExampleRefactoringAction"
               menubarPath="ExampleRefactoringAction"
               enablesFor="1"
               id="ExampleRefactoringAction">
         </action>
      </objectContribution>
   </extension>
 */

public class ExampleRefactoringAction extends Action implements IActionDelegate {

	private IFile fFile;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fFile != null) {
			try {
				ExampleRefactoring refactoring= new ExampleRefactoring(fFile);
				ExampleRefactoringWizard refactoringWizard= new ExampleRefactoringWizard(refactoring, RefactoringWizard.WIZARD_BASED_USER_INTERFACE);
				Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				RefactoringWizardOpenOperation op= new RefactoringWizardOpenOperation(refactoringWizard);
				op.run(shell, "Example refactoring");
			} catch (InterruptedException e) {
				// refactoring got cancelled
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fFile= null;
		if (selection instanceof IStructuredSelection) {
			Object object= ((IStructuredSelection) selection).getFirstElement();
			if (object instanceof IFile) {
				fFile= (IFile) object;
			}
		}
	}

}
