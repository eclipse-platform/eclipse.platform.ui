/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.IMergeContext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryWizard;

/**
 * Partial implementation of a refactoring history merge wizard.
 * <p>
 * This refactoring history wizard executes refactorings from a refactoring
 * history, but uses the time stamps of the refactoring history for the new
 * local refactoring history entries.
 * </p>
 * <p>
 * Additionally, this refactoring wizard collects all added, removed and
 * otherwise changed files which happened during the execution of the
 * refactorings.
 * </p>
 *
 * @since 3.2
 */
public abstract class RefactoringHistoryMergeWizard extends RefactoringHistoryWizard {

	/** Workspace change listener */
	private class WorkspaceChangeListener implements IResourceChangeListener {

		@Override
		public void resourceChanged(final IResourceChangeEvent event) {
			final IResourceDelta delta= event.getDelta();
			if (delta != null) {
				try {
					delta.accept(current -> {
						final IResource resource= current.getResource();
						if (!resource.isDerived()) {
							if (resource.getType() == IResource.FILE) {
								switch (delta.getKind()) {
									case IResourceDelta.ADDED:
										fAddedFiles.add(resource);
										break;
									case IResourceDelta.REMOVED:
										fRemovedFiles.add(resource);
										break;
									case IResourceDelta.CHANGED:
										fChangedFiles.add(resource);
										break;
								}
							}
						}
						return true;
					});
				} catch (CoreException exception) {
					RefactoringUIPlugin.log(exception);
				}
			}
		}
	}

	/** The set of added files */
	private final Set<IResource> fAddedFiles= new HashSet<>();

	/** The set of changed files */
	private final Set<IResource> fChangedFiles= new HashSet<>();

	/** The workspace change listener */
	private final IResourceChangeListener fListener= new WorkspaceChangeListener();

	/** The set of removed files */
	private final Set<IResource> fRemovedFiles= new HashSet<>();

	/**
	 * Creates a new refactoring history merge wizard.
	 *
	 * @param caption
	 *            the caption of the wizard window
	 * @param title
	 *            the title of the overview page
	 * @param description
	 *            the description of the overview page
	 */
	protected RefactoringHistoryMergeWizard(final String caption, final String title, final String description) {
		super(caption, title, description);
	}

	@Override
	protected RefactoringStatus aboutToPerformHistory(final IProgressMonitor monitor) {
		fAddedFiles.clear();
		fRemovedFiles.clear();
		fChangedFiles.clear();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fListener, IResourceChangeEvent.POST_CHANGE);
		return super.aboutToPerformHistory(monitor);
	}

	@Override
	protected RefactoringStatus aboutToPerformRefactoring(final Refactoring refactoring, final RefactoringDescriptor descriptor, final IProgressMonitor monitor) {
		Assert.isNotNull(descriptor);
		final long stamp= descriptor.getTimeStamp();
		if (stamp >= 0)
			RefactoringHistoryService.getInstance().setOverrideTimeStamp(stamp);
		return super.aboutToPerformRefactoring(refactoring, descriptor, monitor);
	}

	/**
	 * Returns the added files.
	 *
	 * @return the added files
	 */
	public Set<IResource> getAddedFiles() {
		return fAddedFiles;
	}

	/**
	 * Returns the changed files.
	 *
	 * @return the changed files
	 */
	public Set<IResource> getChangedFiles() {
		return fChangedFiles;
	}

	/**
	 * Returns the removed files.
	 *
	 * @return the removed files
	 */
	public Set<IResource> getRemovedFiles() {
		return fRemovedFiles;
	}

	@Override
	protected RefactoringStatus historyPerformed(final IProgressMonitor monitor) {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(fListener);
		RefactoringHistoryService.getInstance().setOverrideTimeStamp(-1);
		return super.historyPerformed(monitor);
	}

	@Override
	protected RefactoringStatus refactoringPerformed(final Refactoring refactoring, final IProgressMonitor monitor) {
		RefactoringHistoryService.getInstance().setOverrideTimeStamp(-1);
		return super.refactoringPerformed(refactoring, monitor);
	}

	/**
	 * Resolves the conflicts which have been introduced by the executed
	 * refactorings.
	 *
	 * @param context
	 *            the merge context
	 */
	public void resolveConflicts(final IMergeContext context) {
		Assert.isNotNull(context);
		for (IResource resource : fChangedFiles) {
			final IDiff diff= context.getDiffTree().getDiff(resource);
			if (diff != null) {
				try {
					context.markAsMerged(diff, true, new NullProgressMonitor());
				} catch (CoreException exception) {
					RefactoringUIPlugin.log(exception);
				}
			}
		}
		for (IResource resource : fAddedFiles) {
			final IDiff diff= context.getDiffTree().getDiff(resource);
			if (diff != null) {
				try {
					context.markAsMerged(diff, true, new NullProgressMonitor());
				} catch (CoreException exception) {
					RefactoringUIPlugin.log(exception);
				}
			}
		}
		for (IResource resource : fRemovedFiles) {
			final IDiff diff= context.getDiffTree().getDiff(resource);
			if (diff != null) {
				try {
					context.markAsMerged(diff, true, new NullProgressMonitor());
				} catch (CoreException exception) {
					RefactoringUIPlugin.log(exception);
				}
			}
		}
	}
}