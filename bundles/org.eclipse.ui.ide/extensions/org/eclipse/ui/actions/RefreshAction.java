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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472784
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
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
 * </p>
 * <ul>
 * <li>getSelectedResources - A list containing 0 or more resources to be
 * refreshed</li>
 * <li>updateSelection - controls when this action is enabled</li>
 * <li>refreshResource - can be extended to refresh model objects related to the
 * resource</li>
 * </ul>
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
	@Deprecated
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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.REFRESH_ACTION);
	}
	/**
	 * Checks whether the given project's location has been deleted. If so,
	 * prompts the user with whether to delete the project or not.
	 */
	void checkLocationDeleted(IProject project) throws CoreException {
		if (!project.exists()) {
			return;
		}
		URI locationURI = project.getLocationURI();
		IFileInfo location = IDEResourceInfoUtils.getFileInfo(locationURI);
		if (!location.exists()) {
			String displayedProjectPath = toDisplayPath(locationURI);
			String message = NLS.bind(
					IDEWorkbenchMessages.RefreshAction_locationDeletedMessage,
					project.getName(), displayedProjectPath);

			final MessageDialog dialog = new MessageDialog(getShell(),
					IDEWorkbenchMessages.RefreshAction_dialogTitle, // dialog
					// title
					null, // use default window icon
					message, MessageDialog.QUESTION, 0,
					IDEWorkbenchMessages.DeleteResourceAction_text,
							IDialogConstants.NO_LABEL) {
				@Override
				protected int getShellStyle() {
					return super.getShellStyle() | SWT.SHEET;
				}
			};

			// Must prompt user in UI thread (we're in the operation thread
			// here).
			getShell().getDisplay().syncExec(dialog::open);

			// Do the deletion back in the operation thread
			if (dialog.getReturnCode() == 0) { // yes was chosen
				project.delete(true, true, null);
			}
		}
	}

	private static String toDisplayPath(URI locationURI) {
		IFileStore fileStore = IDEResourceInfoUtils.getFileStore(locationURI);
		return fileStore != null ? fileStore.toString() : locationURI.toString();
	}

	@Override
	protected String getOperationMessage() {
		return IDEWorkbenchMessages.RefreshAction_progressMessage;
	}

	@Override
	protected String getProblemsMessage() {
		return IDEWorkbenchMessages.RefreshAction_problemMessage;
	}

	@Override
	protected String getProblemsTitle() {
		return IDEWorkbenchMessages.RefreshAction_problemTitle;
	}

	/**
	 * Returns a list containing the workspace root if the selection would
	 * otherwise be empty.
	 */
	@Override
	protected List<? extends IResource> getSelectedResources() {
		List<? extends IResource> resources = super.getSelectedResources();
		if (resources.isEmpty()) {
			List<IResource> list = new ArrayList<>();
			list.add(ResourcesPlugin.getWorkspace().getRoot());
			return list;
		}
		return resources;
	}

	/**
	 * The <code>RefreshAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method ensures that this action is
	 * enabled if the selection is empty, but is disabled if any of the selected
	 * elements are not resources.
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection s) {
		return (super.updateSelection(s) || s.isEmpty()) && getSelectedNonResources().isEmpty();
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

	@Override
	final protected IRunnableWithProgress createOperation(final IStatus[] errorStatus) {
		ISchedulingRule rule = null;
		IResourceRuleFactory factory = ResourcesPlugin.getWorkspace().getRuleFactory();

		List<? extends IResource> actionResources = new ArrayList<>(getActionResources());
		if (shouldPerformResourcePruning()) {
			actionResources = pruneResources(actionResources);
		}
		final List<? extends IResource> resources = actionResources;

		Iterator<? extends IResource> res = resources.iterator();
		while (res.hasNext()) {
			rule = MultiRule.combine(rule, factory.refreshRule(res.next()));
		}
		return new WorkspaceModifyOperation(rule) {
			@Override
			public void execute(IProgressMonitor mon) {
				SubMonitor subMonitor = SubMonitor.convert(mon, resources.size());
				MultiStatus errors = null;
				subMonitor.setTaskName(getOperationMessage());
				Iterator<? extends IResource> resourcesEnum = resources.iterator();
				while (resourcesEnum.hasNext()) {
					try {
						IResource resource = resourcesEnum.next();
						refreshResource(resource, subMonitor.split(1));
					} catch (CoreException e) {
						errors = recordError(errors, e);
					}
				}
				if (errors != null) {
					errorStatus[0] = errors;
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
	protected void refreshResource(IResource resource, IProgressMonitor monitor) throws CoreException {
		// Check if project's location has been deleted,
		// as per 1G83UCE: ITPUI:WINNT - Refresh from local doesn't detect new
		// or deleted projects
		// and also for bug report #18283
		if (resource.getType() == IResource.PROJECT) {
			checkLocationDeleted((IProject) resource);
		} else if (resource.getType() == IResource.ROOT) {
			IProject[] projects = ((IWorkspaceRoot) resource).getProjects();
			for (IProject project : projects) {
				checkLocationDeleted(project);
			}
		}
		resource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	@Override
	public void run() {
		final IStatus[] errorStatus = new IStatus[1];
		errorStatus[0] = Status.OK_STATUS;
		final WorkspaceModifyOperation op = (WorkspaceModifyOperation) createOperation(errorStatus);
		WorkspaceJob job = new WorkspaceJob("refresh") { //$NON-NLS-1$

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					op.run(monitor);
				} catch (InvocationTargetException e) {
					String msg = NLS.bind(
							IDEWorkbenchMessages.WorkspaceAction_logTitle, getClass()
									.getName(), e.getTargetException());
					throw new CoreException(StatusUtil.newStatus(IStatus.ERROR, msg, e.getTargetException()));
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
