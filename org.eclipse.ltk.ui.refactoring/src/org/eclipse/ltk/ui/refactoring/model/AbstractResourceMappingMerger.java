/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.diff.ITwoWayDiff;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.IMergeStatus;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.ResourceMappingMerger;
import org.eclipse.team.core.mapping.provider.MergeStatus;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryImplementation;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringHistoryMergeWizard;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * Partial implementation of a refactoring-aware resource mapping merger.
 * <p>
 * This class provides support to determine pending refactorings during model
 * merging and model update, and displays a refactoring wizard to apply the
 * refactorings to the local workspace.
 * </p>
 * <p>
 * Note: this class is designed to be extended by clients. Programming language
 * implementers which need a refactoring-aware resource mapping merger to
 * associated with their model provider may extend this class to implement
 * language-specific project dependency rules.
 * </p>
 *
 * @see org.eclipse.team.core.mapping.IResourceMappingMerger
 *
 * @since 3.2
 */
public abstract class AbstractResourceMappingMerger extends ResourceMappingMerger {

	/** Refactoring history model merge configuration */
	private static final class RefactoringHistoryModelMergeConfiguration extends RefactoringHistoryControlConfiguration {

		/**
		 * Creates a new refactoring history model merge configuration.
		 *
		 * @param project
		 *            the project, or <code>null</code>
		 */
		public RefactoringHistoryModelMergeConfiguration(final IProject project) {
			super(project, false, false);
		}

		/**
		 * {@inheritDoc}
		 */
		public String getProjectPattern() {
			return RefactoringUIMessages.RefactoringModelMerger_project_pattern;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getWorkspaceCaption() {
			return RefactoringUIMessages.RefactoringModelMerger_workspace_caption;
		}
	}

	/** The refactoring history model merge wizard */
	private static final class RefactoringHistoryModelMergeWizard extends RefactoringHistoryMergeWizard {

		/**
		 * Creates a new refactoring history model merge wizard.
		 */
		public RefactoringHistoryModelMergeWizard() {
			super(RefactoringUIMessages.RefactoringWizard_refactoring, RefactoringUIMessages.AbstractRefactoringModelMerger_wizard_title, RefactoringUIMessages.AbstractRefactoringModelMerger_wizard_description);
		}
	}

	/**
	 * Returns the shell of the active workbench window.
	 *
	 * @return the active shell
	 */
	private static Shell getActiveShell() {
		final IWorkbench workbench= PlatformUI.getWorkbench();
		if (workbench != null) {
			final IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
			if (window != null)
				return window.getShell();
		}
		return null;
	}

	/**
	 * Returns the projects affected by the specified refactoring history.
	 *
	 * @param history
	 *            the refactoring history
	 * @return the affected projects, or <code>null</code> if the entire
	 *         workspace is affected
	 */
	private static IProject[] getAffectedProjects(final RefactoringHistory history) {
		final Set set= new HashSet();
		final RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		final IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		for (int index= 0; index < proxies.length; index++) {
			final String name= proxies[index].getProject();
			if (name != null && !"".equals(name)) //$NON-NLS-1$
				set.add(root.getProject(name));
			else
				return null;
		}
		final IProject[] projects= new IProject[set.size()];
		set.toArray(projects);
		return projects;
	}

	/**
	 * Return a shell that can be used by the operation to display dialogs, etc.
	 *
	 * @return a shell
	 */
	private static Shell getDialogShell() {
		final Shell[] shell= new Shell[] { null};
		Display.getDefault().syncExec(new Runnable() {

			public final void run() {
				shell[0]= getActiveShell();
			}
		});
		return shell[0];
	}

	/** The model provider */
	private final ModelProvider fModelProvider;

	/**
	 * Creates a new abstract refactoring model merger.
	 *
	 * @param provider
	 *            the model provider
	 */
	protected AbstractResourceMappingMerger(final ModelProvider provider) {
		Assert.isNotNull(provider);
		fModelProvider= provider;
	}

	/**
	 * Hook method which is called before the actual merge process happens.
	 * <p>
	 * Subclasses may extend this method to perform any special processing. The
	 * default implementation checks whether there are any pending refactorings
	 * in the merge context and displays a refactoring wizard to let the user
	 * perform the pending refactorings before merge.
	 * </p>
	 * <p>
	 * Returning a status of severity {@link IStatus#ERROR} will terminate the
	 * merge process.
	 * </p>
	 *
	 * @param context
	 *            the merge context
	 * @param monitor
	 *            the progress monitor to use
	 * @return a status describing the outcome of the operation
	 */
	protected IStatus aboutToPerformMerge(final IMergeContext context, final IProgressMonitor monitor) {
		Assert.isNotNull(context);
		Assert.isNotNull(monitor);
		try {
			monitor.beginTask(RefactoringUIMessages.RefactoringModelMerger_merge_message, 100);
			final IDiff[] diffs= getDiffs(context);
			final RefactoringHistory history= getRefactoringHistory(diffs, monitor);
			if (history != null && !history.isEmpty()) {
				boolean execute= true;
				final IProject[] projects= getAffectedProjects(history);
				if (projects != null) {
					final IProject[] dependencies= getDependencies(projects);
					if (dependencies.length == 0)
						execute= false;
				}
				if (execute) {
					final Shell shell= getDialogShell();
					shell.getDisplay().syncExec(new Runnable() {

						public final void run() {
							if (MessageDialog.openQuestion(shell, RefactoringUIMessages.RefactoringWizard_refactoring, RefactoringUIMessages.AbstractRefactoringModelMerger_accept_question)) {
								final RefactoringHistoryMergeWizard wizard= new RefactoringHistoryModelMergeWizard();
								int result= Window.OK;
								try {
									wizard.setConfiguration(new RefactoringHistoryModelMergeConfiguration((projects != null && projects.length == 1) ? projects[0] : null));
									wizard.setInput(history);
									result= new WizardDialog(shell, wizard).open();
								} finally {
									if (result != Window.CANCEL)
										wizard.resolveConflicts(context);
								}
							}
						}
					});
				}
			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	/**
	 * Creates a merge status.
	 *
	 * @param context
	 *            the merge context
	 * @param status
	 *            the status
	 * @return the resulting merge status
	 */
	private IStatus createMergeStatus(final IMergeContext context, final IStatus status) {
		if (status.getCode() == IMergeStatus.CONFLICTS)
			return new MergeStatus(status.getPlugin(), status.getMessage(), context.getScope().getMappings(fModelProvider.getDescriptor().getId()));
		return status;
	}

	/**
	 * Returns the dependent projects of the projects associated with the
	 * incoming refactorings.
	 * <p>
	 * Subclasses must implement this method to return the dependent projects
	 * according to the semantics of the associated programming language. The
	 * result of this method is used to decide whether the resource mapping
	 * merger should execute the incoming refactorings in order to fix up
	 * references in dependent projects.
	 * </p>
	 *
	 * @param projects
	 *            the projects associated with the incoming refactorings in the
	 *            synchronization scope.
	 * @return the dependent projects, or an empty array
	 */
	protected abstract IProject[] getDependencies(IProject[] projects);

	/**
	 * Returns the diffs from the merge context.
	 *
	 * @param context
	 *            the merge context
	 * @return the diffs, or an empty array
	 */
	private IDiff[] getDiffs(final IMergeContext context) {
		final ResourceMapping[] mappings= context.getScope().getMappings(fModelProvider.getDescriptor().getId());
		final Set set= new HashSet();
		for (int index= 0; index < mappings.length; index++) {
			final IDiff[] diffs= context.getDiffTree().getDiffs(context.getScope().getTraversals(mappings[index]));
			for (int offset= 0; offset < diffs.length; offset++)
				set.add(diffs[offset]);
		}
		return (IDiff[]) set.toArray(new IDiff[set.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	protected final ModelProvider getModelProvider() {
		return fModelProvider;
	}

	/**
	 * Returns the incoming refactoring history from the diffs.
	 *
	 * @param diffs
	 *            the diffs
	 * @param monitor
	 *            the progress monitor to use
	 * @return the incoming refactoring history
	 */
	private RefactoringHistory getRefactoringHistory(final IDiff[] diffs, final IProgressMonitor monitor) {
		final Set result= new HashSet();
		try {
			monitor.beginTask(RefactoringUIMessages.RefactoringModelMerger_retrieving_refactorings, diffs.length * 2);
			for (int index= 0; index < diffs.length; index++) {
				final IDiff diff= diffs[index];
				if (diff instanceof IThreeWayDiff) {
					final IThreeWayDiff threeWay= (IThreeWayDiff) diff;
					final Set localDescriptors= new HashSet();
					final Set remoteDescriptors= new HashSet();
					final ITwoWayDiff localDiff= threeWay.getLocalChange();
					if (localDiff instanceof IResourceDiff && localDiff.getKind() != IDiff.NO_CHANGE) {
						final IResourceDiff resourceDiff= (IResourceDiff) localDiff;
						final IFileRevision revision= resourceDiff.getAfterState();
						if (revision != null) {
							final String name= revision.getName();
							if (name.equalsIgnoreCase(RefactoringHistoryService.NAME_HISTORY_FILE))
								getRefactoringDescriptors(revision, localDescriptors, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
						}
					}
					final ITwoWayDiff remoteDiff= threeWay.getLocalChange();
					if (remoteDiff instanceof IResourceDiff && remoteDiff.getKind() != IDiff.NO_CHANGE) {
						final IResourceDiff resourceDiff= (IResourceDiff) remoteDiff;
						final IFileRevision revision= resourceDiff.getAfterState();
						if (revision != null) {
							final String name= revision.getName();
							if (name.equalsIgnoreCase(RefactoringHistoryService.NAME_HISTORY_FILE))
								getRefactoringDescriptors(revision, remoteDescriptors, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
						}
					}
					remoteDescriptors.removeAll(localDescriptors);
					result.addAll(remoteDescriptors);
				}
			}
		} finally {
			monitor.done();
		}
		return new RefactoringHistoryImplementation((RefactoringDescriptorProxy[]) result.toArray(new RefactoringDescriptorProxy[result.size()]));
	}

	/**
	 * Retrieves the refactoring descriptors contained in the specified file
	 * revision.
	 *
	 * @param revision
	 *            the file revision
	 * @param descriptors
	 *            the refactoring descriptor set to populate
	 * @param monitor
	 *            the progress monitor to use
	 */
	static final void getRefactoringDescriptors(final IFileRevision revision, final Set descriptors, final IProgressMonitor monitor) {
		try {
			monitor.beginTask(RefactoringUIMessages.RefactoringModelMerger_retrieving_refactorings, 1);
			IStorage storage= null;
			try {
				storage= revision.getStorage(new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
			} catch (CoreException exception) {
				RefactoringUIPlugin.log(exception);
			}
			if (storage != null) {
				InputStream stream= null;
				try {
					stream= storage.getContents();
					final RefactoringHistory history= RefactoringHistoryService.getInstance().readRefactoringHistory(stream, RefactoringDescriptor.MULTI_CHANGE);
					final RefactoringDescriptorProxy[] proxies= history.getDescriptors();
					for (int offset= 0; offset < proxies.length; offset++)
						descriptors.add(proxies[offset].requestDescriptor(null));
				} catch (CoreException exception) {
					RefactoringUIPlugin.log(exception);
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException exception) {
							// Do nothing
						}
					}
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IStatus merge(final IMergeContext context, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(context);
		IStatus status= Status.OK_STATUS;
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			monitor.beginTask(RefactoringUIMessages.RefactoringModelMerger_merge_message, 200);
//			status= aboutToPerformMerge(context, new SubProgressMonitor(monitor, 75));
			if (status.getSeverity() != IStatus.ERROR) {
				final IDiff[] diffs= getDiffs(context);
				status= createMergeStatus(context, context.merge(diffs, false, new SubProgressMonitor(monitor, 100)));
				final int code= status.getCode();
				if (status.getSeverity() != IStatus.ERROR && code != IMergeStatus.CONFLICTS && code != IMergeStatus.INTERNAL_ERROR)
					status= mergePerformed(context, new SubProgressMonitor(monitor, 25));
			}
		} finally {
			monitor.done();
		}
		return status;
	}

	/**
	 * Hook method which is called after the actual merge process happened. This
	 * method is only called if {@link #merge(IMergeContext, IProgressMonitor)}
	 * returns a status with severity less than {@link IStatus#ERROR} and a
	 * status code unequal to {@link IMergeStatus#CONFLICTS} or
	 * {@link IMergeStatus#INTERNAL_ERROR}.
	 * <p>
	 * Subclasses may extend this method to perform any special processing. The
	 * default implementation does nothing.
	 * </p>
	 *
	 * @param context
	 *            the merge context
	 * @param monitor
	 *            the progress monitor to use
	 * @return a status describing the outcome of the operation
	 */
	protected IStatus mergePerformed(final IMergeContext context, final IProgressMonitor monitor) {
		Assert.isNotNull(context);
		Assert.isNotNull(monitor);
		try {
			monitor.beginTask(RefactoringUIMessages.RefactoringModelMerger_merge_message, 1);
			return Status.OK_STATUS;
		} finally {
			monitor.done();
		}
	}
}
