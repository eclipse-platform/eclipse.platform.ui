/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * The abstract superclass for actions whose only role in life is to invoke 
 * core commands on a set of selected resources.
 * When the action is run, all the work is done within a modal progress
 * dialog. It iterates over all selected resources; errors are collected and
 * displayed to the user via a problems dialog at the end of the operation.
 * User requests to cancel the operation are passed along to the core.
 * <p>
 * Subclasses must implement the following methods:
 * <ul>
 *   <li><code>invokeOperation</code> - to perform the operation on one of the 
 *      selected resources</li>
 *   <li><code>getOperationMessage</code> - to furnish a title for the progress
 *      dialog</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may override the following methods:
 * <ul>
 *   <li><code>shouldPerformResourcePruning</code> - reimplement to turn off</li>
 *   <li><code>updateSelection</code> - extend to refine enablement criteria</li>
 *   <li><code>getProblemsTitle</code> - reimplement to furnish a title for the
 *      problems dialog</li>
 *   <li><code>getProblemsMessage</code> - reimplement to furnish a message for 
 *      the problems dialog</li>
 *   <li><code>run</code> - extend to </li>
 * </ul>
 * </p>
 */
public abstract class WorkspaceAction extends SelectionListenerAction {

	/**
	 * Multi status containing the errors detected when running the operation or
	 * <code>null</code> if no errors detected.
	 */
	private MultiStatus errorStatus;

	/**
	 * The shell in which to show the progress and problems dialog.
	 */
	private Shell shell;
/**
 * Creates a new action with the given text.
 *
 * @param shell the shell (for the modal progress dialog and error messages)
 * @param text the string used as the text for the action, 
 *   or <code>null</code> if there is no text
 */
protected WorkspaceAction(Shell shell, String text) {
	super(text);
	Assert.isNotNull(shell);
	this.shell = shell;
}
/**
 * Opens an error dialog to display the given message.
 * <p>
 * Note that this method must be called from UI thread.
 * </p>
 *
 * @param message the message
 */
void displayError(String message) {
	if (message == null) {
		message = WorkbenchMessages.getString("WorkbenchAction.internalError"); //$NON-NLS-1$
	}
	MessageDialog.openError(shell, getProblemsTitle(), message);
}
/**
 * Runs <code>invokeOperation</code> on each of the selected resources, reporting
 * progress and fielding cancel requests from the given progress monitor.
 *
 * @param monitor a progress monitor
 */
final void execute(IProgressMonitor monitor) {
	//1FTIMQN: ITPCORE:WIN - clients required to do too much iteration work
	List resources = getActionResources();
	if (shouldPerformResourcePruning()) {
		resources = pruneResources(resources);
	}
	Iterator resourcesEnum = resources.iterator();

	// 1FV0B3Y: ITPUI:ALL - sub progress monitors granularity issues
	monitor.beginTask("", resources.size() * 1000); //$NON-NLS-1$
	// Fix for bug 31768 - Don't provide a task name in beginTask
	// as it will be appended to each subTask message. Need to
	// call setTaskName as its the only was to assure the task name is
	// set in the monitor (see bug 31824)
	monitor.setTaskName(getOperationMessage());

	try {
		while (resourcesEnum.hasNext()) {
			IResource resource = (IResource)resourcesEnum.next();

			try {
				// 1FV0B3Y: ITPUI:ALL - sub progress monitors granularity issues
				invokeOperation(resource,new SubProgressMonitor(monitor,1000));
			} catch (CoreException e) {
				recordError(e);
			}

			if (monitor.isCanceled())
				throw new OperationCanceledException();
		}
	} finally {
		monitor.done();
	}
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
 */
abstract String getOperationMessage();
/**
 * Returns the string to display for this action's problems dialog.
 * <p>
 * The <code>WorkspaceAction</code> implementation of this method returns a
 * vague message (localized counterpart of something like "The following 
 * problems occurred."). Subclasses may reimplement to provide something more
 * suited to the particular action.
 * </p>
 *
 * @return the problems message
 */
String getProblemsMessage() {
	return WorkbenchMessages.getString("WorkbenchAction.problemsMessage"); //$NON-NLS-1$
}
/**
 * Returns the title for this action's problems dialog.
 * <p>
 * The <code>WorkspaceAction</code> implementation of this method returns a
 * generic title (localized counterpart of "Problems"). Subclasses may 
 * reimplement to provide something more suited to the particular action.
 * </p>
 *
 * @return the problems dialog title
 */
String getProblemsTitle() {
	return WorkbenchMessages.getString("WorkspaceAction.problemsTitle"); //$NON-NLS-1$
}
/**
 * Returns the shell for this action. This shell is used for the modal progress
 * and error dialogs.
 *
 * @return the shell
 */
Shell getShell() {
	return shell;
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
 * @param resource one of the selected resources
 * @param monitor a progress monitor
 * @exception CoreException if the operation fails
 */
abstract void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException;
/**
 * Returns whether the given resource is accessible, where files and folders
 * are always considered accessible, and where a project is accessible iff it
 * is open.
 *
 *	@param resource the resource
 *	@return <code>true</code> if the resource is accessible, and 
 *     <code>false</code> if it is not
 */
boolean isAccessible(IResource resource) {
	switch (resource.getType()) {
		case IResource.FILE:
			return true;
		case IResource.FOLDER:
			return true;
		case IResource.PROJECT:
			return ((IProject)resource).isOpen();
		default:
			return false;
	}
}
/**
 * Returns whether the given resource is a descendent of any of the resources
 * in the given list.
 *
 * @param resources the list of resources (element type: <code>IResource</code>)
 * @param child the resource to check
 * @return <code>true</code> if <code>child</code> is a descendent of any of the
 *   elements of <code>resources</code>
 */
boolean isDescendent(List resources, IResource child) {
	IResource parent = child.getParent();
	return parent != null && (resources.contains(parent) || isDescendent(resources, parent));
}
/**
 * Performs pruning on the given list of resources, as described in 
 * <code>shouldPerformResourcePruning</code>.
 *
 * @param resourceCollection the list of resources (element type: 
 *    <code>IResource</code>)
 * @return the list of resources (element type: <code>IResource</code>)
 *		after pruning. 
 * @see #shouldPerformResourcePruning
 */
List pruneResources(List resourceCollection) {
	List prunedList = new ArrayList(resourceCollection);
	Iterator elementsEnum = prunedList.iterator();
	while (elementsEnum.hasNext()) {
		IResource currentResource = (IResource)elementsEnum.next();
		if (isDescendent(prunedList, currentResource))
			elementsEnum.remove(); //Removes currentResource
	}

	return prunedList;
}
/**
 * Records the core exception to be displayed to the user
 * once the action is finished.
 *
 * @param error a <code>CoreException</code>
 */
private void recordError(CoreException error) {
	if (errorStatus == null)
		errorStatus = new MultiStatus(WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, getProblemsMessage(), error);

	errorStatus.merge(error.getStatus());
}
/**
 * The <code>CoreWrapperAction</code> implementation of this <code>IAction</code>
 * method uses a <code>ProgressMonitorDialog</code> to run the operation. The
 * operation calls <code>execute</code> (which, in turn, calls 
 * <code>invokeOperation</code>). Afterwards, any <code>CoreException</code>s
 * encountered while running the operation are reported to the user via a
 * problems dialog.
 * <p>
 * Subclasses may extend this method.
 * </p>
 */
public void run() {
	try {
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) {
				WorkspaceAction.this.execute(monitor);
			}
		};
		new ProgressMonitorDialog(shell).run(true, true, op);
	} catch (InterruptedException e) {
		return;
	} catch (InvocationTargetException e) {
		// we catch CoreException in execute(), but unexpected runtime exceptions or errors may still occur
		String msg = WorkbenchMessages.format("WorkspaceAction.logTitle", new Object[] {getClass().getName(), e.getTargetException()}); //$NON-NLS-1$
		WorkbenchPlugin.log(msg,StatusUtil.newStatus(IStatus.ERROR, msg, e.getTargetException())); 
		displayError(e.getTargetException().getMessage());
	}

	// If errors occurred, open an Error dialog & build a multi status error for it
	if (errorStatus != null) {
		ErrorDialog.openError(shell,
			getProblemsTitle(),
			null,		// no special message
			errorStatus);
	}
	errorStatus = null;
}
/**
 * Returns whether this action should attempt to optimize the resources being
 * operated on. This kind of pruning makes sense when the operation has depth
 * infinity semantics (when the operation is applied explicitly to a resource
 * then it is also applied implicitly to all the resource's descendents).
 * <p>
 * The <code>WorkspaceAction</code> implementation of this method returns
 * <code>true</code>. Subclasses should reimplement to return <code>false</code>
 * if pruning is not required.
 * </p>
 *
 * @return <code>true</code> if pruning should be performed, 
 *   and <code>false</code> if pruning is not desired
 */
boolean shouldPerformResourcePruning() {
	return true;
}
/**
 * The <code>WorkspaceAction</code> implementation of this
 * <code>SelectionListenerAction</code> method ensures that this action is
 * disabled if any of the selected resources are inaccessible. Subclasses may
 * extend to react to selection changes; however, if the super method returns
 * <code>false</code>, the overriding method should also return <code>false</code>.
 */
protected boolean updateSelection(IStructuredSelection selection) {
	if (!super.updateSelection(selection) || selection.isEmpty()) {
		return false;
	}

	for (Iterator i = getSelectedResources().iterator(); i.hasNext();) {
		IResource r = (IResource) i.next();
		if (!isAccessible(r)) {
			return false;
		}
	}
	return true;
}

/**
 * Returns the elements that the action is to be performed on.
 * By default return the selected resources.
 *
 * @return list of resource elements (element type: <code>IResource</code>)
 */
protected List getActionResources() {
	return getSelectedResources();
}
	
}
