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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.history.ExportRefactoringHistoryDialog;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryDialogConfiguration;
import org.eclipse.ltk.internal.ui.refactoring.scripting.ScriptingMessages;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * Action to create a refactoring script from the refactoring history.
 * 
 * @since 3.2
 */
public final class CreateRefactoringScriptAction implements IWorkbenchWindowActionDelegate {

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
		final IRefactoringHistoryService service= RefactoringCore.getRefactoringHistoryService();
		try {
			service.connect();
			final IProgressService progress= PlatformUI.getWorkbench().getProgressService();
			IRunnableContext context= fWindow;
			if (context == null)
				context= progress;
			final RefactoringHistory[] history= { null};
			progress.runInUI(context, new IRunnableWithProgress() {

				public final void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					history[0]= service.getWorkspaceHistory(monitor);
				}
			}, ResourcesPlugin.getWorkspace().getRoot());
			final ExportRefactoringHistoryDialog dialog= new ExportRefactoringHistoryDialog(fWindow.getShell(), new RefactoringHistoryDialogConfiguration(null, true, true) {

				public final String getButtonLabel() {
					return IDialogConstants.OK_LABEL;
				}

				public final String getDialogTitle() {
					return ScriptingMessages.CreateRefactoringScriptAction_dialog_title;
				}

			}, history[0], IDialogConstants.OK_ID);
			dialog.open();
		} catch (InvocationTargetException exception) {
			RefactoringUIPlugin.log(exception);
		} catch (InterruptedException exception) {
			// Do nothing
		} finally {
			service.disconnect();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(final IAction action, final ISelection selection) {
		// Do nothing
	}
}