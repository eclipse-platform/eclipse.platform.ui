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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.scripting.ShowRefactoringHistoryWizard;

/**
 * Action to show the global refactoring history.
 *
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @since 3.3
 */
public final class ShowRefactoringHistoryAction implements IWorkbenchWindowActionDelegate {

	/** The wizard height */
	private static final int SIZING_WIZARD_HEIGHT= 560;

	/** The wizard width */
	private static final int SIZING_WIZARD_WIDTH= 480;

	/**
	 * Shows the refactoring history wizard.
	 *
	 * @param window
	 *            the workbench window
	 */
	private static void showRefactoringHistoryWizard(final IWorkbenchWindow window) {
		Assert.isNotNull(window);
		final ShowRefactoringHistoryWizard wizard= new ShowRefactoringHistoryWizard();
		IRunnableContext context= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (context == null)
			context= PlatformUI.getWorkbench().getProgressService();
		try {
			context.run(false, true, monitor -> {
				final IRefactoringHistoryService service= RefactoringCore.getHistoryService();
				try {
					service.connect();
					wizard.setRefactoringHistory(service.getWorkspaceHistory(monitor));
				} finally {
					service.disconnect();
				}
			});
		} catch (InvocationTargetException exception) {
			RefactoringUIPlugin.log(exception);
		} catch (InterruptedException exception) {
			return;
		}
		final WizardDialog dialog= new WizardDialog(window.getShell(), wizard) {

			@Override
			protected final void createButtonsForButtonBar(final Composite parent) {
				super.createButtonsForButtonBar(parent);
				getButton(IDialogConstants.FINISH_ID).setText(IDialogConstants.OK_LABEL);
			}
		};
		dialog.create();
		dialog.getShell().setSize(Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IRefactoringHelpContextIds.REFACTORING_SHOW_HISTORY_PAGE);
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
			showRefactoringHistoryWizard(fWindow);
		}
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		// Do nothing
	}
}
