/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.history.IRefactoringDescriptorDeleteQuery;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.WorkbenchRunnableAdapter;
import org.eclipse.ltk.ui.refactoring.history.IRefactoringHistoryControl;

/**
 * Helper class to implement shared functionality to edit refactoring histories.
 *
 * @since 3.2
 */
public final class RefactoringHistoryEditHelper {

	/** Interface for refactoring history providers */
	public interface IRefactoringHistoryProvider {

		/**
		 * Returns the refactoring history to use.
		 *
		 * @param monitor
		 *            the progress monitor to use
		 * @return the refactoring history
		 */
		public RefactoringHistory getRefactoringHistory(IProgressMonitor monitor);
	}

	/**
	 * Returns the projects affected by the specified refactoring descriptors.
	 *
	 * @param descriptors
	 *            the refactoring descriptors
	 * @return the affected projects, or <code>null</code> if the entire
	 *         workspace is affected
	 */
	private static IProject[] getAffectedProjects(final RefactoringDescriptorProxy[] descriptors) {
		final Set set= new HashSet();
		for (int index= 0; index < descriptors.length; index++) {
			final String project= descriptors[index].getProject();
			if (project == null || "".equals(project)) //$NON-NLS-1$
				return null;
			set.add(project);
		}
		final IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		final IProject[] result= new IProject[set.size()];
		int index= 0;
		for (final Iterator iterator= set.iterator(); iterator.hasNext(); index++) {
			result[index]= root.getProject((String) iterator.next());
		}
		return result;
	}

	/**
	 * Prompts the user to delete refactorings from the history.
	 *
	 * @param shell
	 *            the shell to use
	 * @param context
	 *            the runnable context to use
	 * @param control
	 *            the refactoring history control
	 * @param query
	 *            the refactoring descriptor delete query to use
	 * @param provider
	 *            the refactoring history provider to use
	 * @param descriptors
	 *            the refactoring descriptors to delete
	 */
	public static void promptRefactoringDelete(final Shell shell, final IRunnableContext context, final IRefactoringHistoryControl control, final IRefactoringDescriptorDeleteQuery query, final IRefactoringHistoryProvider provider, final RefactoringDescriptorProxy[] descriptors) {
		Assert.isNotNull(shell);
		Assert.isNotNull(context);
		Assert.isNotNull(control);
		Assert.isNotNull(query);
		Assert.isNotNull(provider);
		Assert.isNotNull(descriptors);
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		try {
			service.connect();
			try {
				final IProject[] affected= getAffectedProjects(descriptors);
				context.run(false, true, new WorkbenchRunnableAdapter(new IWorkspaceRunnable() {

					public void run(final IProgressMonitor monitor) throws CoreException {
						try {
							monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_deleting_refactorings, 300);
							try {
								service.deleteRefactoringDescriptors(descriptors, query, new SubProgressMonitor(monitor, 280, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
							} catch (CoreException exception) {
								final Throwable throwable= exception.getStatus().getException();
								if (throwable instanceof IOException) {
									shell.getDisplay().syncExec(new Runnable() {

										public void run() {
											MessageDialog.openError(shell, RefactoringUIMessages.ChangeExceptionHandler_refactoring, throwable.getLocalizedMessage());
										}
									});
								} else
									throw exception;
							}
							if (query.hasDeletions()) {
								final RefactoringHistory history= provider.getRefactoringHistory(new SubProgressMonitor(monitor, 20, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
								shell.getDisplay().syncExec(new Runnable() {

									public void run() {
										control.setInput(history);
										control.setCheckedDescriptors(RefactoringPropertyPage.EMPTY_DESCRIPTORS);
									}
								});
							}
						} finally {
							monitor.done();
						}
					}
				}, affected == null ? ResourcesPlugin.getWorkspace().getRoot() : (ISchedulingRule) new MultiRule(affected)));
			} catch (InvocationTargetException exception) {
				RefactoringUIPlugin.log(exception);
			} catch (InterruptedException exception) {
				// Do nothing
			}
		} finally {
			service.disconnect();
		}
	}

	/**
	 * Creates a new refactoring history edit helper
	 */
	private RefactoringHistoryEditHelper() {
		// Not for instantiation
	}
}