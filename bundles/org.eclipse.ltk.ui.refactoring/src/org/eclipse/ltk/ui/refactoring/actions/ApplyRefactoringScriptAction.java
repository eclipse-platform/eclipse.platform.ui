/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.ui.refactoring.actions;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.scripting.ApplyRefactoringScriptWizard;

/**
 * Action to apply a refactoring script to the workspace.
 *
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @since 3.3
 */
public final class ApplyRefactoringScriptAction implements IWorkbenchWindowActionDelegate {

	/** The wizard height */
	private static final int SIZING_WIZARD_HEIGHT= 520;

	/** The wizard width */
	private static final int SIZING_WIZARD_WIDTH= 470;

	/**
	 * Shows the apply script wizard.
	 *
	 * @param window
	 *            the workbench window
	 */
	private static void showApplyScriptWizard(final IWorkbenchWindow window) {
		Assert.isNotNull(window);
		final IWorkbenchWizard wizard= new ApplyRefactoringScriptWizard();
		final ISelection selection= window.getSelectionService().getSelection();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structured= (IStructuredSelection) selection;
			wizard.init(window.getWorkbench(), structured);
		} else
			wizard.init(window.getWorkbench(), null);
		final WizardDialog dialog= new WizardDialog(window.getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IRefactoringHelpContextIds.REFACTORING_APPLY_SCRIPT_PAGE);
		dialog.open();
	}

	/** The workbench window, or <code>null</code> */
	private IWorkbenchWindow fWindow= null;

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public void init(final IWorkbenchWindow window) {
		fWindow= window;
	}

	@Override
	public void run(final IAction action) {
		if (fWindow != null) {
			showApplyScriptWizard(fWindow);
		}
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		// Do nothing
	}
}
