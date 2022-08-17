/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 462760
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.eclipse.ui.progress.IProgressConstants2;

/**
 * The abstract superclass for actions which invoke commands implemented in
 * org.eclipse.core.* on a set of selected resources.
 *
 * It iterates over all selected resources; errors are collected and displayed
 * to the user via a problems dialog at the end of the operation. User requests
 * to cancel the operation are passed along to the core.
 * <p>
 * Subclasses must implement the following methods:
 * </p>
 * <ul>
 * <li><code>invokeOperation</code> - to perform the operation on one of the
 * selected resources</li>
 * <li><code>getOperationMessage</code> - to furnish a title for the progress
 * dialog</li>
 * </ul>
 * <p>
 * Subclasses may override the following methods:
 * </p>
 * <ul>
 * <li><code>shouldPerformResourcePruning</code> - reimplement to turn off</li>
 * <li><code>updateSelection</code> - extend to refine enablement criteria</li>
 * <li><code>getProblemsTitle</code> - reimplement to furnish a title for the
 * problems dialog</li>
 * <li><code>getProblemsMessage</code> - reimplement to furnish a message for
 * the problems dialog</li>
 * <li><code>run</code> - extend to</li>
 * </ul>
 */
public abstract class WorkspaceAction extends SelectionListenerAction {
	/**
	 * The shell in which to show the progress and problems dialog.
	 */
	private final IShellProvider shellProvider;

	/**
	 * Creates a new action with the given text.
	 *
	 * @param shell
	 *            the shell (for the modal progress dialog and error messages)
	 * @param text
	 *            the string used as the text for the action, or
	 *            <code>null</code> if there is no text
	 * @deprecated See {@link #WorkspaceAction(IShellProvider, String)}
	 */
	@Deprecated
	protected WorkspaceAction(final Shell shell, String text) {
		super(text);
		Assert.isNotNull(shell);
		shellProvider = () -> shell;
	}

	/**
	 * Creates a new action with the given text.
	 *
	 * @param provider
	 *            the shell provider (for the modal progress dialog and error
	 *            messages)
	 * @param text
	 *            the string used as the text for the action, or
	 *            <code>null</code> if there is no text
	 * @since 3.4
	 */
	protected WorkspaceAction(IShellProvider provider, String text) {
		super(text);
		Assert.isNotNull(provider);
		shellProvider = provider;
	}

	/**
	 * Opens an error dialog to display the given message.
	 * <p>
	 * Note that this method must be called from UI thread.
	 * </p>
	 *
	 * @param message
	 *            the message
	 */
	void displayError(String message) {
		if (message == null) {
			message = IDEWorkbenchMessages.WorkbenchAction_internalError;
		}
		MessageDialog.openError(shellProvider.getShell(), getProblemsTitle(), message);
	}

	/**
	 * Runs <code>invokeOperation</code> on each of the selected resources,
	 * reporting progress and fielding cancel requests from the given progress
	 * monitor.
	 * <p>
	 * Note that if an action is running in the background, the same action
	 * instance can be executed multiple times concurrently. This method must
	 * not access or modify any mutable state on action class.
	 *
	 * @param monitor
	 *            a progress monitor
	 * @return The result of the execution
	 */
	final IStatus execute(List<? extends IResource> resources, IProgressMonitor mon) {
		MultiStatus errors = null;
		// 1FTIMQN: ITPCORE:WIN - clients required to do too much iteration work
		if (shouldPerformResourcePruning()) {
			resources = pruneResources(resources);
		}
		SubMonitor subMonitor = SubMonitor.convert(mon, resources.size());
		// Fix for bug 31768 - Don't provide a task name in beginTask
		// as it will be appended to each subTask message. Need to
		// call setTaskName as its the only was to assure the task name is
		// set in the monitor (see bug 31824)
		subMonitor.setTaskName(getOperationMessage());
		for (IResource resource : resources) {
			try {
				invokeOperation(resource, subMonitor.split(1));
			} catch (CoreException e) {
				errors = recordError(errors, e);
			}
		}
		return errors == null ? Status.OK_STATUS : errors;
	}

	/**
	 * Returns the string to display for this action's operation.
	 * <p>
	 * Note that this hook method is invoked in a non-UI thread.
	 * </p>
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @return the message
	 *
	 * @since 3.1
	 */
	protected abstract String getOperationMessage();

	/**
	 * Returns the string to display for this action's problems dialog.
	 * <p>
	 * The <code>WorkspaceAction</code> implementation of this method returns
	 * a vague message (localized counterpart of something like "The following
	 * problems occurred."). Subclasses may reimplement to provide something
	 * more suited to the particular action.
	 * </p>
	 *
	 * @return the problems message
	 *
	 * @since 3.1
	 */
	protected String getProblemsMessage() {
		return IDEWorkbenchMessages.WorkbenchAction_problemsMessage;
	}

	/**
	 * Returns the title for this action's problems dialog.
	 * <p>
	 * The <code>WorkspaceAction</code> implementation of this method returns
	 * a generic title (localized counterpart of "Problems"). Subclasses may
	 * reimplement to provide something more suited to the particular action.
	 * </p>
	 *
	 * @return the problems dialog title
	 *
	 * @since 3.1
	 */
	protected String getProblemsTitle() {
		return IDEWorkbenchMessages.WorkspaceAction_problemsTitle;
	}

	/**
	 * Returns the shell for this action. This shell is used for the modal
	 * progress and error dialogs.
	 *
	 * @return the shell
	 */
	Shell getShell() {
		return shellProvider.getShell();
	}

	/**
	 * Performs this action's operation on each of the selected resources, reporting
	 * progress to, and fielding cancel requests from, the given progress monitor.
	 * <p>
	 * Note that this method is invoked in a non-UI thread.
	 * </p>
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @deprecated Since 3.3, subclasses should instead implement the method
	 *             {@link #createOperation(IStatus[])} and provide an empty
	 *             implementation for this method.
	 *
	 * @param resource one of the selected resources
	 * @param monitor  a progress monitor
	 * @exception CoreException if the operation fails
	 *
	 * @since 3.1
	 */
	@Deprecated
	protected void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {

	}

	/**
	 * Returns whether the given resource is a descendent of any of the
	 * resources in the given list.
	 *
	 * @param resources
	 *            the list of resources (element type: <code>IResource</code>)
	 * @param child
	 *            the resource to check
	 * @return <code>true</code> if <code>child</code> is a descendent of
	 *         any of the elements of <code>resources</code>
	 */
	boolean isDescendent(List<IResource> resources, IResource child) {
		IResource parent = child.getParent();
		return parent != null && (resources.contains(parent) || isDescendent(resources, parent));
	}

	/**
	 * Performs pruning on the given list of resources, as described in
	 * <code>shouldPerformResourcePruning</code>.
	 *
	 * @param resourceCollection
	 *            the list of resources (element type: <code>IResource</code>)
	 * @return the list of resources (element type: <code>IResource</code>)
	 *         after pruning.
	 * @see #shouldPerformResourcePruning
	 */
	List<IResource> pruneResources(List<? extends IResource> resourceCollection) {
		List<IResource> prunedList = new ArrayList<>(resourceCollection);
		Iterator<IResource> elementsEnum = prunedList.iterator();
		while (elementsEnum.hasNext()) {
			IResource currentResource = elementsEnum.next();
			if (isDescendent(prunedList, currentResource)) {
				elementsEnum.remove(); // Removes currentResource
			}
		}
		return prunedList;
	}

	/**
	 * Records the core exception to be displayed to the user once the action is
	 * finished.
	 *
	 * @param error
	 *            a <code>CoreException</code>
	 */
	MultiStatus recordError(MultiStatus errors, CoreException error) {
		if (errors == null) {
			errors = new MultiStatus(IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.ERROR, getProblemsMessage());
		}
		errors.merge(error.getStatus());
		return errors;
	}

	/**
	 * The <code>CoreWrapperAction</code> implementation of this
	 * <code>IAction</code> method uses a <code>ProgressMonitorDialog</code>
	 * to run the operation. The operation calls <code>execute</code> (which,
	 * in turn, calls <code>invokeOperation</code>). Afterwards, any
	 * <code>CoreException</code>s encountered while running the operation
	 * are reported to the user via a problems dialog.
	 * <p>
	 * Subclasses may extend this method.
	 * </p>
	 */
	@Override
	public void run() {
		IStatus[] errorStatus = new IStatus[1];
		try {
			new ProgressMonitorJobsDialog(shellProvider.getShell()).run(true, true, createOperation(errorStatus));
		} catch (InterruptedException e) {
			return;
		} catch (InvocationTargetException e) {
			// we catch ExecutionException in the created operation, but unexpected runtime
			// exceptions or errors may still occur
			String msg = NLS.bind(
					IDEWorkbenchMessages.WorkspaceAction_logTitle, getClass()
							.getName(), e.getTargetException());
			IDEWorkbenchPlugin.log(msg, StatusUtil.newStatus(IStatus.ERROR, msg, e.getTargetException()));
			displayError(e.getTargetException().getMessage());
		}
		// If errors occurred, open an Error dialog & build a multi status error
		// for it
		if (errorStatus[0] != null && !errorStatus[0].isOK()) {
			// no special message
			ErrorDialog.openError(shellProvider.getShell(), getProblemsTitle(), null, errorStatus[0]);
		}
	}

	/**
	 * Returns whether this action should attempt to optimize the resources
	 * being operated on. This kind of pruning makes sense when the operation
	 * has depth infinity semantics (when the operation is applied explicitly to
	 * a resource then it is also applied implicitly to all the resource's
	 * descendents).
	 * <p>
	 * The <code>WorkspaceAction</code> implementation of this method returns
	 * <code>true</code>. Subclasses should reimplement to return
	 * <code>false</code> if pruning is not required.
	 * </p>
	 *
	 * @return <code>true</code> if pruning should be performed, and
	 *         <code>false</code> if pruning is not desired
	 *
	 * @since 3.1
	 */
	protected boolean shouldPerformResourcePruning() {
		return true;
	}

	/**
	 * The <code>WorkspaceAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method ensures that this action is
	 * disabled if any of the selected resources are inaccessible. Subclasses
	 * may extend to react to selection changes; however, if the super method
	 * returns <code>false</code>, the overriding method should also return
	 * <code>false</code>.
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection) || selection.isEmpty()) {
			return false;
		}
		for (IResource r : getSelectedResources()) {
			if (!r.isAccessible()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the elements that the action is to be performed on. By default
	 * return the selected resources.
	 * <p>
	 * Subclasses may override this method.
	 *
	 * @return list of resource elements (element type: <code>IResource</code>)
	 */
	protected List<? extends IResource> getActionResources() {
		return getSelectedResources();
	}

	/**
	 * Run the action in the background rather than with the progress dialog.
	 *
	 * @param rule
	 *            The rule to apply to the background job or <code>null</code>
	 *            if there isn't one.
	 */
	public void runInBackground(ISchedulingRule rule) {
		runInBackground(rule, (Object[]) null);
	}

	/**
	 * Run the action in the background rather than with the progress dialog.
	 *
	 * @param rule
	 *            The rule to apply to the background job or <code>null</code>
	 *            if there isn't one.
	 * @param jobFamily
	 *            a single family that the job should belong to or
	 *            <code>null</code> if none.
	 *
	 * @since 3.1
	 */
	public void runInBackground(ISchedulingRule rule, Object jobFamily) {
		if (jobFamily == null) {
			runInBackground(rule, (Object[]) null);
		} else {
			runInBackground(rule, new Object[] { jobFamily });
		}
	}

	/**
	 * Run the action in the background rather than with the progress dialog.
	 *
	 * @param rule
	 *            The rule to apply to the background job or <code>null</code>
	 *            if there isn't one.
	 * @param jobFamilies
	 *            the families the job should belong to or <code>null</code>
	 *            if none.
	 *
	 * @since 3.1
	 */
	public void runInBackground(ISchedulingRule rule, final Object[] jobFamilies) {
		// obtain a copy of the selected resources before the job is forked
		final List<IResource> resources = new ArrayList<>(getActionResources());
		Job job = new WorkspaceJob(removeMnemonics(getText())) {

			@Override
			public boolean belongsTo(Object family) {
				if (jobFamilies == null || family == null) {
					return false;
				}
				for (Object jobFamily : jobFamilies) {
					if (family.equals(jobFamily)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				return WorkspaceAction.this.execute(resources, monitor);
			}
		};
		if (rule != null) {
			job.setRule(rule);
		}
		if (job.belongsTo(ResourcesPlugin.FAMILY_MANUAL_BUILD)) {
			job.setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
		}
		job.setUser(true);
		job.schedule();
	}

	/**
	 * Returns the operation to perform when this action runs. The returned
	 * operation must be an {@link IRunnableWithProgress} that will perform the
	 * action's work. The default implementation returns an operation that will
	 * iterate over the selected resources and call
	 * {@link #invokeOperation(IResource, IProgressMonitor)} for each resource.
	 * Subclasses must either implement
	 * {@link #invokeOperation(IResource, IProgressMonitor)} or override this
	 * method to provide a different operation. Subclasses typically override
	 * this method when an undoable operation is to be provided.
	 *
	 * @param errorStatus
	 *            an array of error status objects to which the result of
	 *            running the operation should be added.
	 *
	 * @return the operation to perform when this action runs.
	 * @since 3.3
	 */
	protected IRunnableWithProgress createOperation(final IStatus[] errorStatus) {
		return new WorkspaceModifyOperation() {
			@Override
			public void execute(IProgressMonitor monitor) {
				errorStatus[0] = WorkspaceAction.this.execute(getActionResources(), monitor);
			}
		};
	}

}
