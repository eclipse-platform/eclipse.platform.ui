/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.actions;

import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.scripting.ApplyRefactoringScriptWizard;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;

/**
 * Action to apply a refactoring script to the workspace.
 * 
 * @since 3.2
 */
public final class ApplyRefactoringScriptAction implements IWorkbenchWindowActionDelegate {

	/** The wizard height */
	private static final int SIZING_WIZARD_HEIGHT= 520;

	/** The wizard width */
	private static final int SIZING_WIZARD_WIDTH= 470;

	/** The workbench window, or <code>null</code> */
	private IWorkbenchWindow fWindow= null;

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(final IWorkbenchWindow window) {
		fWindow= window;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run(final IAction action) {
		if (fWindow != null) {
			final ISelection selection= fWindow.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structured= (IStructuredSelection) selection;
				final IWorkbenchWizard wizard= new ApplyRefactoringScriptWizard();
				wizard.init(fWindow.getWorkbench(), structured);
				final WizardDialog dialog= new WizardDialog(fWindow.getShell(), wizard);
				dialog.create();
				dialog.getShell().setSize(Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT);
				PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IRefactoringHelpContextIds.REFACTORING_APPLY_SCRIPT_PAGE);
				dialog.open();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(final IAction action, final ISelection selection) {
		// Do nothing
	}
}