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
package org.eclipse.ltk.internal.ui.refactoring.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.ui.mapping.IMergeContext;
import org.eclipse.team.ui.mapping.IMergeStatus;
import org.eclipse.team.ui.mapping.IResourceMappingMerger;
import org.eclipse.team.ui.operations.MergeStatus;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryImplementation;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryWizard;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * Resource mapping merger for refactoring histories.
 * 
 * @since 3.2
 */
public class RefactoringModelMerger implements IResourceMappingMerger {

	/** Refactoring history control configuration */
	private static final class RefactoringHistoryPreviewConfiguration extends RefactoringHistoryControlConfiguration {

		/**
		 * Creates a new refactoring history preview configuration.
		 * 
		 * @param project
		 *            the project, or <code>null</code>
		 * @param time
		 *            <code>true</code> to display time information,
		 *            <code>false</code> otherwise
		 */
		public RefactoringHistoryPreviewConfiguration(final IProject project, final boolean time) {
			super(project, time);
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
	 * Returns the affected projects by the history.
	 * 
	 * @param history
	 *            the refactoring history
	 * @return the affected projects
	 */
	private static IProject[] getAffectedProjects(final RefactoringHistory history) {
		final Set set= new HashSet();
		final RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		final IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		for (int index= 0; index < proxies.length; index++) {
			final String name= proxies[index].getProject();
			if (name != null)
				set.add(root.getProject(name));
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

	/**
	 * Returns the incoming refactoring history from the sync info tree.
	 * 
	 * @param tree
	 *            the sync info tree
	 * @param monitor
	 *            the progress monitor to use
	 * @return the incoming refactoring history
	 */
	private static RefactoringHistory getRefactoringHistory(final SyncInfoTree tree, final IProgressMonitor monitor) {
		try {
			monitor.beginTask(RefactoringUIMessages.RefactoringModelMerger_retrieving_refactorings, tree.size());
			final Collection existing= new ArrayList();
			final Set incoming= new HashSet();
			for (final Iterator iterator= tree.iterator(); iterator.hasNext();) {
				final SyncInfo info= (SyncInfo) iterator.next();
				final int direction= SyncInfo.getDirection(info.getKind());
				if (direction == SyncInfo.INCOMING || direction == SyncInfo.CONFLICTING) {
					final IResourceVariant variant= info.getRemote();
					if (variant != null) {
						final String name= variant.getName();
						if (name.equalsIgnoreCase(RefactoringHistoryService.NAME_INDEX_FILE)) {
							final IResource resource= info.getLocal();
							if (resource instanceof IStorage && resource.exists()) {
								final IStorage storage= (IStorage) resource;
								if (storage != null) {
									InputStream stream= null;
									try {
										stream= storage.getContents();
										final RefactoringDescriptorProxy[] proxies= RefactoringHistoryService.getInstance().readRefactoringDescriptorProxies(stream);
										for (int index= 0; index < proxies.length; index++)
											existing.add(proxies[index]);
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
							}
						} else if (name.equalsIgnoreCase(RefactoringHistoryService.NAME_HISTORY_FILE)) {
							IStorage storage= null;
							try {
								storage= variant.getStorage(new SubProgressMonitor(monitor, 1));
							} catch (TeamException exception) {
								RefactoringUIPlugin.log(exception);
							}
							if (storage != null) {
								InputStream stream= null;
								try {
									stream= storage.getContents();
									final RefactoringHistory history= RefactoringHistoryService.getInstance().readRefactoringHistory(stream);
									if (history != null && !history.isEmpty()) {
										final RefactoringDescriptorProxy[] proxies= history.getDescriptors();
										for (int index= 0; index < proxies.length; index++)
											incoming.add(proxies[index]);
									}
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
						}
					}
				}
			}
			incoming.removeAll(existing);
			final RefactoringDescriptorProxy[] proxies= new RefactoringDescriptorProxy[incoming.size()];
			incoming.toArray(proxies);
			return new RefactoringHistoryImplementation(proxies);
		} finally {
			monitor.done();
		}
	}

	/** The model provider */
	private final ModelProvider fModelProvider;

	/**
	 * Creates a new refactoring model merger.
	 * 
	 * @param provider
	 *            the model provider
	 */
	public RefactoringModelMerger(final ModelProvider provider) {
		Assert.isNotNull(provider);
		fModelProvider= provider;
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
	 * Returns the sync info tree computed from the context.
	 * 
	 * @param context
	 *            the merge context
	 * @return the sync info tree
	 */
	private SyncInfoTree getSyncInfoTree(final IMergeContext context) {
		final ResourceMapping[] mappings= context.getScope().getMappings(fModelProvider.getDescriptor().getId());
		final SyncInfoTree tree= new SyncInfoTree();
		try {
			tree.beginInput();
			for (int index= 0; index < mappings.length; index++) {
				final SyncInfo[] infos= context.getSyncInfoTree().getSyncInfos(context.getScope().getTraversals(mappings[index]));
				for (int offset= 0; offset < infos.length; offset++)
					tree.add(infos[offset]);
			}
		} finally {
			tree.endInput(new NullProgressMonitor());
		}
		return tree;
	}

	/**
	 * {@inheritDoc}
	 */
	public IStatus merge(final IMergeContext context, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(context);
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			monitor.beginTask(RefactoringUIMessages.RefactoringModelMerger_merge_message, 200);
			final SyncInfoTree tree= getSyncInfoTree(context);
			final RefactoringHistory history= getRefactoringHistory(tree, new SubProgressMonitor(monitor, 100));
			if (history != null && !history.isEmpty()) {
				final IProject[] projects= getAffectedProjects(history);
				final Shell shell= getDialogShell();
				shell.getDisplay().syncExec(new Runnable() {

					public final void run() {
						new WizardDialog(shell, new RefactoringHistoryWizard(history, new RefactoringHistoryPreviewConfiguration(projects.length == 1 ? projects[0] : null, true))).open();
					}
				});
			}
			return createMergeStatus(context, context.merge(tree, new SubProgressMonitor(monitor, 100)));
		} finally {
			monitor.done();
		}
	}
}
