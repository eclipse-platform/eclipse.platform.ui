/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.scripting.CreateRefactoringScriptWizard;
import org.eclipse.ltk.internal.ui.refactoring.scripting.ScriptingMessages;

/**
 * Action to open the dialog to create a refactoring script from the refactoring history.
 *
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @since 3.3
 */
public final class CreateRefactoringScriptAction implements IWorkbenchWindowActionDelegate {

	/** The wizard height */
	private static final int SIZING_WIZARD_HEIGHT= 610;

	/** The wizard width */
	private static final int SIZING_WIZARD_WIDTH= 500;

	/**
	 * Shows the create script wizard.
	 *
	 * @param window
	 *            the workbench window
	 */
	private static void showCreateScriptWizard(final IWorkbenchWindow window) {
		Assert.isNotNull(window);
		final CreateRefactoringScriptWizard wizard= new CreateRefactoringScriptWizard();
		try {
			IRunnableContext context= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (context == null)
				context= PlatformUI.getWorkbench().getProgressService();
			context.run(false, true, new IRunnableWithProgress() {

				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final IRefactoringHistoryService service= RefactoringCore.getHistoryService();
					try {
						service.connect();
						wizard.setRefactoringHistory(service.getWorkspaceHistory(monitor));
					} finally {
						service.disconnect();
					}
				}
			});
		} catch (InvocationTargetException exception) {
			RefactoringUIPlugin.log(exception);
		} catch (InterruptedException exception) {
			return;
		}
		final WizardDialog dialog= new WizardDialog(window.getShell(), wizard) {

			protected final void createButtonsForButtonBar(final Composite parent) {
				super.createButtonsForButtonBar(parent);
				getButton(IDialogConstants.FINISH_ID).setText(ScriptingMessages.CreateRefactoringScriptAction_finish_button_label);
			}
		};
		dialog.create();
		dialog.getShell().setSize(Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IRefactoringHelpContextIds.REFACTORING_CREATE_SCRIPT_PAGE);
		dialog.open();
	}

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
			showCreateScriptWizard(fWindow);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(final IAction action, final ISelection selection) {
		// Do nothing
	}
}
