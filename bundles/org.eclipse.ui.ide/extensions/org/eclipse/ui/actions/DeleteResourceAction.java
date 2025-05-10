/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Benjamin Muskalla <b.muskalla@gmx.net> - Bug 172574
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 41431, 462760
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472784
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.ZipFileUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.DeleteResourcesOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.actions.LTKLauncher;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Standard action for deleting the currently selected resources.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DeleteResourceAction extends SelectionListenerAction {

	static class DeleteProjectDialog extends MessageDialog {

		private List<? extends IResource> projects;

		private boolean deleteContent;

		/**
		 * Control testing mode. In testing mode, it returns true to delete
		 * contents and does not pop up the dialog.
		 */
		private boolean fIsTesting;

		private Button radio1;

		private Button radio2;

		DeleteProjectDialog(Shell parentShell, List<? extends IResource> projects) {
			super(parentShell, getTitle(projects), null, // accept the
					// default window
					// icon
					getMessage(projects), MessageDialog.QUESTION, 0,
							IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL);

			this.projects = projects;
			setShellStyle(getShellStyle() | SWT.SHEET);
		}

		static String getTitle(List<? extends IResource> projects) {
			if (projects.size() == 1) {
				return IDEWorkbenchMessages.DeleteResourceAction_titleProject1;
			}
			return IDEWorkbenchMessages.DeleteResourceAction_titleProjectN;
		}

		static String getMessage(List<? extends IResource> projects) {
			if (projects.size() == 1) {
				IProject project = (IProject) projects.get(0);
				return NLS
						.bind(
								IDEWorkbenchMessages.DeleteResourceAction_confirmProject1,
								project.getName());
			}
			return NLS.bind(
					IDEWorkbenchMessages.DeleteResourceAction_confirmProjectN,
					Integer.valueOf((projects.size())));
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IIDEHelpContextIds.DELETE_PROJECT_DIALOG);
		}

		@Override
		protected Control createCustomArea(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			radio1 = new Button(composite, SWT.RADIO);
			radio1.addSelectionListener(selectionListener);
			String text1;
			if (projects.size() == 1) {
				IProject project = (IProject) projects.get(0);
				if (project == null || project.getLocation() == null) {
					text1 = IDEWorkbenchMessages.DeleteResourceAction_deleteContentsN;
				} else {
					text1 = NLS
							.bind(
									IDEWorkbenchMessages.DeleteResourceAction_deleteContents1,
									project.getLocation().toOSString());
				}
			} else {
				text1 = IDEWorkbenchMessages.DeleteResourceAction_deleteContentsN;
			}
			radio1.setText(text1);
			radio1.setFont(parent.getFont());

			// Add explanatory label that the action cannot be undone.
			// We can't put multi-line formatted text in a radio button,
			// so we have to create a separate label.
			Label detailsLabel = new Label(composite, SWT.LEFT);
			detailsLabel.setText(IDEWorkbenchMessages.DeleteResourceAction_deleteContentsDetails);
			detailsLabel.setFont(parent.getFont());
			// indent the explanatory label
			GridData data = new GridData();
			data.horizontalIndent = 20;
			detailsLabel.setLayoutData(data);
			// add a listener so that clicking on the label selects the
			// corresponding radio button.
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172574
			detailsLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					deleteContent = true;
					radio1.setSelection(deleteContent);
					radio2.setSelection(!deleteContent);
				}
			});
			// Add a spacer label
			new Label(composite, SWT.LEFT);

			radio2 = new Button(composite, SWT.RADIO);
			radio2.addSelectionListener(selectionListener);
			String text2 = IDEWorkbenchMessages.DeleteResourceAction_doNotDeleteContents;
			radio2.setText(text2);
			radio2.setFont(parent.getFont());

			// set initial state
			radio1.setSelection(deleteContent);
			radio2.setSelection(!deleteContent);

			return composite;
		}

		private SelectionListener selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				if (button.getSelection()) {
					deleteContent = (button == radio1);
				}
			}
		};

		boolean getDeleteContent() {
			return deleteContent;
		}

		@Override
		public int open() {
			// Override Window#open() to allow for non-interactive testing.
			if (fIsTesting) {
				deleteContent = true;
				return Window.OK;
			}
			return super.open();
		}

		/**
		 * Set this delete dialog into testing mode. It won't pop up, and it
		 * returns true for deleteContent.
		 *
		 * @param t
		 *            the testing mode
		 */
		void setTestingMode(boolean t) {
			fIsTesting = t;
		}
	}

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".DeleteResourceAction";//$NON-NLS-1$

	private IShellProvider shellProvider;

	/**
	 * Whether or not we are deleting content for projects.
	 */
	private boolean deleteContent;

	/**
	 * Flag that allows testing mode ... it won't pop up the project delete
	 * dialog, and will return "delete all content".
	 */
	protected boolean fTestingMode;

	private String[] modelProviderIds;

	/**
	 * Creates a new delete resource action.
	 *
	 * @param shell
	 *            the shell for any dialogs
	 * @deprecated Should take an IShellProvider, see
	 *             {@link #DeleteResourceAction(IShellProvider)}
	 */
	@Deprecated
	public DeleteResourceAction(final Shell shell) {
		super(IDEWorkbenchMessages.DeleteResourceAction_text);
		Assert.isNotNull(shell);
		initAction();
		setShellProvider(() -> shell);
	}

	/**
	 * Creates a new delete resource action.
	 *
	 * @param provider
	 *            the shell provider to use. Must not be <code>null</code>.
	 * @since 3.4
	 */
	public DeleteResourceAction(IShellProvider provider) {
		super(IDEWorkbenchMessages.DeleteResourceAction_text);
		Assert.isNotNull(provider);
		initAction();
		setShellProvider(provider);
	}

	/**
	 * Action initialization.
	 */
	private void initAction() {
		setToolTipText(IDEWorkbenchMessages.DeleteResourceAction_toolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.DELETE_RESOURCE_ACTION);
		setId(ID);
	}

	private void setShellProvider(IShellProvider provider) {
		shellProvider = provider;
	}

	/**
	 * Returns whether delete can be performed on the current selection.
	 *
	 * @param resources
	 *            the selected resources
	 * @return <code>true</code> if the resources can be deleted, and
	 *         <code>false</code> if the selection contains non-resources or
	 *         phantom resources
	 */
	private boolean canDelete(List<? extends IResource> resources) {
		if (resources.isEmpty()) {
			return false;
		}

		// allow only projects or only non-projects to be selected;
		// note that the selection may contain multiple types of resource
		if (!(containsOnlyProjects(resources) || containsOnlyNonProjects(resources))) {
			return false;
		}

		// Return false if at least one element is not existing or workspace
		// root
		for (IResource resource : resources) {
			if (resource.isPhantom() || resource.getType() == IResource.ROOT) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns whether the selection contains linked resources.
	 *
	 * @param resources
	 *            the selected resources
	 * @return <code>true</code> if the resources contain linked resources,
	 *         and <code>false</code> otherwise
	 */
	private boolean containsLinkedResource(List<? extends IResource> resources) {
		for (int i = 0; i < resources.size(); i++) {
			IResource resource = resources.get(i);
			if (resource.isLinked() && !ZipFileUtil.isOpenZipFile(resource.getLocationURI())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether the selection contains only non-projects.
	 *
	 * @param resources
	 *            the selected resources
	 * @return <code>true</code> if the resources contains only non-projects,
	 *         and <code>false</code> otherwise
	 */
	private boolean containsOnlyNonProjects(List<? extends IResource> resources) {
		int types = getSelectedResourceTypes(resources);
		// check for empty selection
		if (types == 0) {
			return false;
		}
		// note that the selection may contain multiple types of resource
		return (types & IResource.PROJECT) == 0;
	}

	/**
	 * Returns whether the selection contains only projects.
	 *
	 * @param resources
	 *            the selected resources
	 * @return <code>true</code> if the resources contains only projects, and
	 *         <code>false</code> otherwise
	 */
	private boolean containsOnlyProjects(List<? extends IResource> resources) {
		int types = getSelectedResourceTypes(resources);
		// note that the selection may contain multiple types of resource
		return types == IResource.PROJECT;
	}

	/**
	 * Asks the user to confirm a delete operation.
	 *
	 * @param resources
	 *            the selected resources
	 * @return <code>true</code> if the user says to go ahead, and
	 *         <code>false</code> if the deletion should be abandoned
	 */
	private boolean confirmDelete(List<? extends IResource> resources) {
		if (containsOnlyProjects(resources)) {
			return confirmDeleteProjects(resources);
		}
		return confirmDeleteNonProjects(resources);

	}

	/**
	 * Asks the user to confirm a delete operation, where the selection contains
	 * no projects.
	 *
	 * @param resources
	 *            the selected resources
	 * @return <code>true</code> if the user says to go ahead, and
	 *         <code>false</code> if the deletion should be abandoned
	 */
	private boolean confirmDeleteNonProjects(List<? extends IResource> resources) {
		String title;
		String msg;
		if (resources.size() == 1) {
			title = IDEWorkbenchMessages.DeleteResourceAction_title1;
			IResource resource = resources.get(0);
			if (resource.isLinked() && !ZipFileUtil.isOpenZipFile(resource.getLocationURI())) {
				msg = NLS
						.bind(
								IDEWorkbenchMessages.DeleteResourceAction_confirmLinkedResource1,
								resource.getName());
			} else {
				msg = NLS.bind(
						IDEWorkbenchMessages.DeleteResourceAction_confirm1,
						resource.getName());
			}
		} else {
			title = IDEWorkbenchMessages.DeleteResourceAction_titleN;
			if (containsLinkedResource(resources)) {
				msg = NLS.bind(IDEWorkbenchMessages.DeleteResourceAction_confirmLinkedResourceN, resources.size());
			} else {
				msg = NLS.bind(IDEWorkbenchMessages.DeleteResourceAction_confirmN, resources.size());
			}
		}
		return MessageDialog.openQuestion(shellProvider.getShell(), title, msg);
	}

	/**
	 * Asks the user to confirm a delete operation, where the selection contains
	 * only projects. Also remembers whether project content should be deleted.
	 *
	 * @param resources
	 *            the selected resources
	 * @return <code>true</code> if the user says to go ahead, and
	 *         <code>false</code> if the deletion should be abandoned
	 */
	private boolean confirmDeleteProjects(List<? extends IResource> resources) {
		DeleteProjectDialog dialog = new DeleteProjectDialog(shellProvider.getShell(), resources);
		dialog.setTestingMode(fTestingMode);
		int code = dialog.open();
		deleteContent = dialog.getDeleteContent();
		return code == 0; // YES
	}

	/**
	 * Returns a bit-mask containing the types of resources in the selection.
	 *
	 * @param resources
	 *            the selected resources
	 */
	private int getSelectedResourceTypes(List<? extends IResource> resources) {
		int types = 0;
		for (int i = 0; i < resources.size(); i++) {
			types |= resources.get(i).getType();
		}
		return types;
	}

	@Override
	public void run() {
		final List<? extends IResource> resources = getSelectedResources();
		if (resources.isEmpty()) {
			return;
		}

		if (!fTestingMode) {
			if (LTKLauncher.openDeleteWizard(getStructuredSelection())) {
				CloseResourceAction.closeMatchingEditors(resources, true);
				return;
			}
		}

		// WARNING: do not query the selected resources more than once
		// since the selection may change during the run,
		// e.g. due to window activation when the prompt dialog is dismissed.
		// For more details, see Bug 60606 [Navigator] (data loss) Navigator
		// deletes/moves the wrong file
		if (!confirmDelete(resources)) {
			return;
		}

		Job deletionCheckJob = new Job(IDEWorkbenchMessages.DeleteResourceAction_checkJobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (resources.isEmpty()) {
					return Status.CANCEL_STATUS;
				}
				scheduleDeleteJob(resources);
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				if (IDEWorkbenchMessages.DeleteResourceAction_jobName.equals(family)) {
					return true;
				}
				return super.belongsTo(family);
			}
		};

		deletionCheckJob.schedule();

	}

	/**
	 * Schedule a job to delete the resources to delete.
	 */
	private void scheduleDeleteJob(final List<? extends IResource> resourcesToDelete) {
		// use a non-workspace job with a runnable inside so we can avoid
		// periodic updates
		Job deleteJob = new Job(IDEWorkbenchMessages.DeleteResourceAction_jobName) {
			@Override
			public IStatus run(final IProgressMonitor monitor) {
				try {
					final DeleteResourcesOperation op =
						new DeleteResourcesOperation(resourcesToDelete.toArray(new IResource[resourcesToDelete.size()]),
								IDEWorkbenchMessages.DeleteResourceAction_operationLabel, deleteContent);
					op.setModelProviderIds(getModelProviderIds());
					// If we are deleting projects and their content, do not
					// execute the operation in the undo history, since it cannot be
					// properly restored.  Just execute it directly so it won't be
					// added to the undo history.
					if (deleteContent && containsOnlyProjects(resourcesToDelete)) {
						// We must compute the execution status first so that any user prompting
						// or validation checking occurs.  Do it in a syncExec because
						// we are calling this from a Job.
						WorkbenchJob statusJob = new WorkbenchJob("Status checking"){ //$NON-NLS-1$
							@Override
							public IStatus runInUIThread(IProgressMonitor m) {
								return op.computeExecutionStatus(m);
							}

						};

						statusJob.setSystem(true);
						statusJob.schedule();
						try {//block until the status is ready
							statusJob.join();
						} catch (InterruptedException e) {
							//Do nothing as status will be a cancel
						}

						if (statusJob.getResult().isOK()) {
							return op.execute(monitor, WorkspaceUndoUtil.getUIInfoAdapter(shellProvider.getShell()));
						}
						return statusJob.getResult();
					}
					return PlatformUI.getWorkbench().getOperationSupport()
							.getOperationHistory().execute(op, monitor,
							WorkspaceUndoUtil.getUIInfoAdapter(shellProvider.getShell()));
				} catch (ExecutionException e) {
					if (e.getCause() instanceof CoreException) {
						return ((CoreException)e.getCause()).getStatus();
					}
					return new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, e.getMessage(),e);
				}
			}

			@Override
			public boolean belongsTo(Object family) {
				if (IDEWorkbenchMessages.DeleteResourceAction_jobName.equals(family)) {
					return true;
				}
				return super.belongsTo(family);
			}

		};
		deleteJob.setUser(true);
		deleteJob.schedule();
	}

	/**
	 * The <code>DeleteResourceAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method disables the action if the
	 * selection contains phantom resources or non-resources
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection) && canDelete(getSelectedResources());
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
