/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

/**
 * Standard action for refreshing the workspace from the local file system for
 * the selected resources and all of their descendants.
 * <p>
 * This class may be instantiated; it may also subclass to extend:
 * <ul>
 * <li>getSelectedResources - A list containing 0 or more resources to be
 * refreshed</li>
 * <li>updateSelection - controls when this action is enabled</li>
 * <li>refreshResource - can be extended to refresh model objects related to
 * the resource</li>
 * <ul>
 * </p>
 */
public class RefreshAction extends WorkspaceAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".RefreshAction";//$NON-NLS-1$

	/**
	 * Creates a new action.
	 * 
	 * @param shell
	 *            the shell for any dialogs
	 * @deprecated See {@link #RefreshAction(IShellProvider)}
	 */
	public RefreshAction(Shell shell) {
		super(shell, IDEWorkbenchMessages.RefreshAction_text);
		initAction();
	}

	/**
	 * Creates a new action.
	 * 
	 * @param provider
	 *            the IShellProvider for any dialogs.
	 * @since 3.4
	 */
	public RefreshAction(IShellProvider provider){
		super(provider, IDEWorkbenchMessages.RefreshAction_text);
		initAction();
	}
	
	/**
	 * Initializes for the constructor.
	 */
	private void initAction(){
		setToolTipText(IDEWorkbenchMessages.RefreshAction_toolTip);
		setId(ID);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.REFRESH_ACTION);
	}
	/**
	 * Checks whether the given project's location has been deleted. If so,
	 * prompts the user with whether to delete the project or not.
	 */
	void checkLocationDeleted(IProject project) throws CoreException {
		if (!project.exists()) {
			return;
		}
		IFileInfo location = IDEResourceInfoUtils.getFileInfo(project
				.getLocationURI());
		if (!location.exists()) {
			String message = NLS.bind(
					IDEWorkbenchMessages.RefreshAction_locationDeletedMessage,
					project.getName(), location.toString());

			final MessageDialog dialog = new MessageDialog(getShell(),
					IDEWorkbenchMessages.RefreshAction_dialogTitle, // dialog
					// title
					null, // use default window icon
					message, MessageDialog.QUESTION, new String[] {
							IDialogConstants.YES_LABEL,
							IDialogConstants.NO_LABEL }, 0) {
				protected int getShellStyle() {
					return super.getShellStyle() | SWT.SHEET;
				}
			}; // yes is the
			// default

			// Must prompt user in UI thread (we're in the operation thread
			// here).
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					dialog.open();
				}
			});

			// Do the deletion back in the operation thread
			if (dialog.getReturnCode() == 0) { // yes was chosen
				project.delete(true, true, null);
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected String getOperationMessage() {
		return IDEWorkbenchMessages.RefreshAction_progressMessage;
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected String getProblemsMessage() {
		return IDEWorkbenchMessages.RefreshAction_problemMessage;
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected String getProblemsTitle() {
		return IDEWorkbenchMessages.RefreshAction_problemTitle;
	}

	/**
	 * Returns a list containing the workspace root if the selection would
	 * otherwise be empty.
	 */
	protected List getSelectedResources() {
		List resources = super.getSelectedResources();
		if (resources.isEmpty()) {
			resources = new ArrayList();
			resources.add(ResourcesPlugin.getWorkspace().getRoot());
		}
		return resources;
	}

	/**
	 * The <code>RefreshAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method ensures that this action is
	 * enabled if the selection is empty, but is disabled if any of the selected
	 * elements are not resources.
	 */
	protected boolean updateSelection(IStructuredSelection s) {
		return (super.updateSelection(s) || s.isEmpty())
				&& getSelectedNonResources().size() == 0;
	}

	/**
	 * Handle the key release.
	 * 
	 * @param event
	 *            the event
	 */
	public void handleKeyReleased(KeyEvent event) {

		if (event.keyCode == SWT.F5 && event.stateMask == 0) {
			refreshAll();
		}
	}

	/**
	 * Refreshes the entire workspace.
	 */
	final public void refreshAll() {
		IStructuredSelection currentSelection = getStructuredSelection();
		selectionChanged(StructuredSelection.EMPTY);
		run();
		selectionChanged(currentSelection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.WorkspaceAction#createOperation(org.eclipse.core.runtime.IStatus[])
	 */
	final protected IRunnableWithProgress createOperation(
			final IStatus[] errorStatus) {
		ISchedulingRule rule = null;
		IResourceRuleFactory factory = ResourcesPlugin.getWorkspace()
				.getRuleFactory();

		List actionResources = new ArrayList(getActionResources());
		if (shouldPerformResourcePruning()) {
			actionResources = pruneResources(actionResources);
		}
		final List resources = actionResources;

		Iterator res = resources.iterator();
		while (res.hasNext()) {
			rule = MultiRule.combine(rule, factory.refreshRule((IResource) res
					.next()));
		}
		return new WorkspaceModifyOperation(rule) {
			public void execute(IProgressMonitor monitor) {
				MultiStatus errors = null;
				monitor.beginTask("", resources.size() * 1000); //$NON-NLS-1$
				monitor.setTaskName(getOperationMessage());
				Iterator resourcesEnum = resources.iterator();
				try {
					while (resourcesEnum.hasNext()) {
						try {
							IResource resource = (IResource) resourcesEnum
									.next();
							refreshResource(resource, new SubProgressMonitor(
									monitor, 1000));
						} catch (CoreException e) {
							errors = recordError(errors, e);
						}
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
					}
					if (errors != null) {
						errorStatus[0] = errors;
					}
				} finally {
					monitor.done();
				}
			}
		};
	}

	/**
	 * Refresh the resource (with a check for deleted projects).
	 * <p>
	 * This method may be extended to refresh model objects related to the
	 * resource.
	 * </p>
	 * 
	 * @param resource
	 *            the resource to refresh. Must not be <code>null</code>.
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 *             if things go wrong
	 * @since 3.4
	 */
	protected void refreshResource(IResource resource, IProgressMonitor monitor)
			throws CoreException {
		// Check if project's location has been deleted,
		// as per 1G83UCE: ITPUI:WINNT - Refresh from local doesn't detect new
		// or deleted projects
		// and also for bug report #18283
		if (resource.getType() == IResource.PROJECT) {
			checkLocationDeleted((IProject) resource);
		} else if (resource.getType() == IResource.ROOT) {
			IProject[] projects = ((IWorkspaceRoot) resource).getProjects();
			for (int i = 0; i < projects.length; i++) {
				checkLocationDeleted(projects[i]);
			}
		}
		resource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.WorkspaceAction#run()
	 */
	public void run() {
		final IStatus[] errorStatus = new IStatus[1];
		errorStatus[0] = Status.OK_STATUS;
		final WorkspaceModifyOperation op = (WorkspaceModifyOperation) createOperation(errorStatus);
		WorkspaceJob job = new WorkspaceJob("refresh") { //$NON-NLS-1$

			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				try {
					op.run(monitor);
				} catch (InvocationTargetException e) {
					String msg = NLS.bind(
							IDEWorkbenchMessages.WorkspaceAction_logTitle, getClass()
									.getName(), e.getTargetException());
					throw new CoreException(StatusUtil.newStatus(IStatus.ERROR,
							msg, e.getTargetException()));
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
				return errorStatus[0];
			}
			
		};
		ISchedulingRule rule = op.getRule();
		if (rule != null) {
			job.setRule(rule);
		}
		job.setUser(true);
		job.schedule();
	}
}
