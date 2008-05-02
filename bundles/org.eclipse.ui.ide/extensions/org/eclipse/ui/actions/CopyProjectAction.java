/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import java.net.URI;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ProjectLocationSelectionDialog;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The CopyProjectAction is the action designed to copy projects specifically as
 * they have different semantics from other resources. Note that this action
 * assumes that a single project is selected and being manipulated. This should
 * be disabled for multi select or no selection.
 */
public class CopyProjectAction extends SelectionListenerAction {
	private static String COPY_TOOL_TIP = IDEWorkbenchMessages.CopyProjectAction_toolTip;

	private static String COPY_TITLE = IDEWorkbenchMessages.CopyProjectAction_title;

	private static String PROBLEMS_TITLE = IDEWorkbenchMessages.CopyProjectAction_copyFailedTitle;

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".CopyProjectAction";//$NON-NLS-1$

	/**
	 * The shell in which to show any dialogs.
	 * @deprecated the shell provider should be used instead.
	 */
	protected Shell shell;

	/**
	 * The IShellProvider in which to show any dialogs.
	 * @since 3.4
	 */
	protected IShellProvider shellProvider;

	/**
	 * Status containing the errors detected when running the operation or
	 * <code>null</code> if no errors detected.
	 */
	protected IStatus errorStatus;

	private String[] modelProviderIds;

	/**
	 * Creates a new project copy action with the default text.
	 * 
	 * @param shell
	 *            the shell for any dialogs
	 * 
	 * @deprecated {@link #CopyProjectAction(IShellProvider)}
	 */
	public CopyProjectAction(Shell shell) {
		this(shell, COPY_TITLE);
	}

	/**
	 * Creates a new project copy action with the default text.
	 * 
	 * @param provider
	 * 				the IShellProvider for any dialogs
	 * @since 3.4
	 */
	public CopyProjectAction(IShellProvider provider){
		this(provider, COPY_TITLE);
	}
	
	/**
	 * Creates a new project copy action with the given text.
	 * 
	 * @param shell
	 *            the shell for any dialogs
	 * @param name
	 *            the string used as the text for the action, or
	 *            <code>null</code> if there is no text
	 * 
	 * @deprecated {@link #CopyProjectAction(IShellProvider, String)}
	 */
	CopyProjectAction(final Shell shell, String name) {
		super(name);
		Assert.isNotNull(shell);
		shellProvider = new IShellProvider() {
			public Shell getShell() {
				return shell;
			} };
			initAction();
	}
	
	/**
	 * 
	 * @param provider
	 *            the IShellProvider for any dialogs
	 * @param name
	 *            the string used as the text for the action, or
	 *            <code>null</code> if there is no text
	 */
	CopyProjectAction(IShellProvider provider, String name){
		super(name);
		Assert.isNotNull(provider);
		shellProvider = provider;
		initAction();
	}
	
	private void initAction(){
		shell = shellProvider.getShell();
		setToolTipText(COPY_TOOL_TIP);
		setId(CopyProjectAction.ID);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.COPY_PROJECT_ACTION);
	}

	/**
	 * Create a new IProjectDescription for the copy using the name and path
	 * selected from the dialog.
	 * 
	 * @return IProjectDescription
	 * @param project
	 *            the source project
	 * @param projectName
	 *            the name for the new project
	 * @param rootLocation
	 *            the path the new project will be stored under.
	 */
	protected IProjectDescription createDescription(IProject project,
			String projectName, IPath rootLocation) throws CoreException {
		// Get a copy of the current description and modify it
		IProjectDescription newDescription = project.getDescription();
		newDescription.setName(projectName);

		// If the location is the default then set the location to null
		if (rootLocation.equals(Platform.getLocation())) {
			newDescription.setLocation(null);
		} else {
			newDescription.setLocation(rootLocation);
		}

		return newDescription;
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
		MessageDialog.openError(this.shellProvider.getShell(), getErrorsTitle(), message);
	}

	/**
	 * Return the title of the errors dialog.
	 * 
	 * @return java.lang.String
	 * 
	 * @deprecated As of 3.3, the undoable operation created by this action
	 *             handles error dialogs.
	 */
	protected String getErrorsTitle() {
		return PROBLEMS_TITLE;
	}

	/**
	 * Get the plugin used by a copy action
	 * 
	 * @return AbstractUIPlugin
	 */
	protected org.eclipse.ui.plugin.AbstractUIPlugin getPlugin() {
		return (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
	}

	/**
	 * Copies the project to the new values.
	 * 
	 * @param project
	 *            the project to copy
	 * @param projectName
	 *            the name of the copy
	 * @param newLocation
	 *            URI
	 * @return <code>true</code> if the copy operation completed, and
	 *         <code>false</code> if it was abandoned part way
	 */
	boolean performCopy(final IProject project, final String projectName,
			final URI newLocation) {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				org.eclipse.ui.ide.undo.CopyProjectOperation op = new org.eclipse.ui.ide.undo.CopyProjectOperation(
						project, projectName, newLocation, getText());
				op.setModelProviderIds(getModelProviderIds());
				try {
					PlatformUI.getWorkbench().getOperationSupport()
							.getOperationHistory().execute(op, monitor,
									WorkspaceUndoUtil.getUIInfoAdapter(shellProvider.getShell()));
				} catch (ExecutionException e) {
					if (e.getCause() instanceof CoreException) {
						recordError((CoreException)e.getCause());
					} else {
						IDEWorkbenchPlugin.log(e.getMessage(), e);
						displayError(e.getMessage());
					}
				}
			}
		};

		try {
			new ProgressMonitorJobsDialog(shellProvider.getShell()).run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			displayError(NLS.bind(
					IDEWorkbenchMessages.CopyProjectAction_internalError, e
							.getTargetException().getMessage()));
			return false;
		}

		return true;
	}

	/**
	 * Query for a new project name and destination using the parameters in the
	 * existing project.
	 * 
	 * @return Object [] or null if the selection is cancelled
	 * @param project
	 *            the project we are going to copy.
	 */
	protected Object[] queryDestinationParameters(IProject project) {
		ProjectLocationSelectionDialog dialog = new ProjectLocationSelectionDialog(
				shellProvider.getShell(), project);
		dialog.setTitle(IDEWorkbenchMessages.CopyProjectAction_copyTitle);
		dialog.open();
		return dialog.getResult();
	}

	/**
	 * Records the core exception to be displayed to the user once the action is
	 * finished.
	 * 
	 * @param error
	 *            a <code>CoreException</code>
	 */
	final void recordError(CoreException error) {
		this.errorStatus = error.getStatus();
	}

	/**
	 * Implementation of method defined on <code>IAction</code>.
	 */
	public void run() {

		errorStatus = null;

		IProject project = (IProject) getSelectedResources().get(0);

		// Get the project name and location in a two element list
		Object[] destinationPaths = queryDestinationParameters(project);
		if (destinationPaths == null) {
			return;
		}

		String newName = (String) destinationPaths[0];
		URI newLocation = URIUtil.toURI((String) destinationPaths[1]);

		boolean completed = performCopy(project, newName, newLocation);

        if (!completed) {
			return; // not appropriate to show errors
		}

        // If errors occurred, open an Error dialog
        if (errorStatus != null) {
            ErrorDialog.openError(this.shellProvider.getShell(), getErrorsTitle(), null,
                    errorStatus);
            errorStatus = null;
        }
	}

	/**
	 * The <code>CopyResourceAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method enables this action only if
	 * there is a single selection which is a project.
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection)) {
			return false;
		}
		if (getSelectedNonResources().size() > 0) {
			return false;
		}

		// to enable this command there must be one project selected and nothing
		// else
		List selectedResources = getSelectedResources();
		if (selectedResources.size() != 1) {
			return false;
		}
		IResource source = (IResource) selectedResources.get(0);
		if (source instanceof IProject && ((IProject) source).isOpen()) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the model provider ids that are known to the client that
	 * instantiated this operation.
	 * 
	 * @return the model provider ids that are known to the client that
	 *         instantiated this operation.
	 * @since 3.2
	 */
	public String[] getModelProviderIds() {
		return modelProviderIds;
	}

	/**
	 * Sets the model provider ids that are known to the client that
	 * instantiated this operation. Any potential side effects reported by these
	 * models during validation will be ignored.
	 * 
	 * @param modelProviderIds
	 *            the model providers known to the client who is using this
	 *            operation.
	 * @since 3.2
	 */
	public void setModelProviderIds(String[] modelProviderIds) {
		this.modelProviderIds = modelProviderIds;
	}
}
