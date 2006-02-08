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
package org.eclipse.ltk.ui.refactoring.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffVisitor;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.diff.ITwoWayDiff;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.ui.mapping.SynchronizationContentProvider;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryImplementation;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringDescriptorDiff;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringDescriptorSynchronizationProxy;

/**
 * Partial implementation of a refactoring-aware synchronization content
 * provider.
 * <p>
 * This class provides a method
 * {@link #getRefactorings(ISynchronizationContext, IProject, IProgressMonitor)}
 * which may be used in subclasses to render refactorings in team
 * synchronization views.
 * </p>
 * <p>
 * Note: this class is designed to be extended by clients. Programming language
 * implementers who need refactoring support in a synchronization content
 * provider used in team syncrhonization views may use this class as a basis for
 * refactoring-aware synchronization content providers.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider
 * 
 * @since 3.2
 */
public abstract class AbstractSynchronizationContentProvider extends SynchronizationContentProvider {

	/**
	 * Gets the refactoring descriptor proxies stored in the specified file
	 * revision.
	 * 
	 * @param revision
	 *            the file revision
	 * @param set
	 *            the set of refactoring descriptor proxies
	 * @param direction
	 *            the direction
	 * @param monitor
	 *            the progress monitor to use
	 */
	private void getRefactorings(final IFileRevision revision, final Set set, final int direction, final IProgressMonitor monitor) {
		IStorage storage= null;
		try {
			storage= revision.getStorage(new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
		} catch (CoreException exception) {
			RefactoringUIPlugin.log(exception);
		}
		if (storage != null) {
			InputStream stream= null;
			final IRefactoringHistoryService service= RefactoringCore.getRefactoringHistoryService();
			try {
				service.connect();
				stream= storage.getContents();
				final RefactoringHistory history= service.readRefactoringHistory(stream, RefactoringDescriptor.MULTI_CHANGE);
				if (history != null && !history.isEmpty()) {
					final RefactoringDescriptorProxy[] proxies= history.getDescriptors();
					for (int offset= 0; offset < proxies.length; offset++) {
						final RefactoringDescriptorSynchronizationProxy proxy= new RefactoringDescriptorSynchronizationProxy(proxies[offset], direction);
						if (!set.contains(proxy))
							set.add(proxy);
						else
							set.remove(proxy);
					}
				}
			} catch (CoreException exception) {
				RefactoringUIPlugin.log(exception);
			} finally {
				service.disconnect();
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

	/**
	 * Returns the refactorings for the specified project which are not in sync.
	 * <p>
	 * This method fetches refactoring information for all refactorings which
	 * are not in sync for a project (e.g. have not yet been checked into the
	 * respository, or are pending refactorings to execute on the local
	 * workspace).
	 * </p>
	 * 
	 * @param context
	 *            the synchronization context to use
	 * @param project
	 *            the project to compute its refactorings
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code> if no
	 *            progress monitoring or cancellation is desired
	 * @return the refactoring history representing the refactorings
	 */
	protected RefactoringHistory getRefactorings(final ISynchronizationContext context, final IProject project, IProgressMonitor monitor) {
		Assert.isNotNull(context);
		Assert.isNotNull(project);
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			monitor.beginTask(RefactoringUIMessages.RefactoringModelMerger_retrieving_refactorings, 12);
			final IProgressMonitor finalMonitor= monitor;
			final Set remote= new HashSet();
			final Set local= new HashSet();
			try {
				final IResourceDiffTree tree= context.getDiffTree();
				tree.accept(project.getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER).getFullPath(), new IDiffVisitor() {

					public final boolean visit(final IDiff diff) {
						if (diff instanceof IThreeWayDiff) {
							final IThreeWayDiff threeWay= (IThreeWayDiff) diff;
							final IResource resource= tree.getResource(diff);
							if (resource.getName().equals(RefactoringHistoryService.NAME_HISTORY_FILE) && resource.getType() == IResource.FILE) {
								final ITwoWayDiff remoteDiff= threeWay.getRemoteChange();
								if (remoteDiff instanceof IResourceDiff && remoteDiff.getKind() != IDiff.NO_CHANGE) {
									final IFileRevision afterRevision= ((IResourceDiff) remoteDiff).getAfterState();
									if (afterRevision != null)
										getRefactorings(afterRevision, remote, IThreeWayDiff.INCOMING, finalMonitor);
									final IFileRevision beforeRevision= ((IResourceDiff) remoteDiff).getBeforeState();
									if (beforeRevision != null)
										getRefactorings(beforeRevision, remote, IThreeWayDiff.INCOMING, finalMonitor);
								}
								final ITwoWayDiff localDiff= threeWay.getLocalChange();
								if (localDiff instanceof IResourceDiff && localDiff.getKind() != IDiff.NO_CHANGE) {
									final IFileRevision beforeRevision= ((IResourceDiff) localDiff).getBeforeState();
									if (beforeRevision != null)
										getRefactorings(beforeRevision, local, IThreeWayDiff.OUTGOING, finalMonitor);
									final IFileRevision afterRevision= ((IResourceDiff) localDiff).getAfterState();
									if (afterRevision != null)
										getRefactorings(afterRevision, local, IThreeWayDiff.OUTGOING, finalMonitor);
								}
							}
						}
						return true;
					}
				}, IResource.DEPTH_INFINITE);
			} catch (CoreException exception) {
				RefactoringUIPlugin.log(exception);
			}
			for (final Iterator iterator= local.iterator(); iterator.hasNext();) {
				final RefactoringDescriptorProxy proxy= (RefactoringDescriptorProxy) iterator.next();
				if (!remote.contains(proxy))
					remote.add(proxy);
				else
					remote.remove(proxy);
			}
			for (final Iterator iterator= remote.iterator(); iterator.hasNext();) {
				final RefactoringDescriptorSynchronizationProxy proxy= (RefactoringDescriptorSynchronizationProxy) iterator.next();
				if (!isVisible(new RefactoringDescriptorDiff(proxy, IDiff.CHANGE, proxy.getDirection())))
					remote.remove(proxy);
			}
			final RefactoringDescriptorProxy[] proxies= new RefactoringDescriptorProxy[remote.size()];
			remote.toArray(proxies);
			return new RefactoringHistoryImplementation(proxies);
		} finally {
			monitor.done();
		}
	}
}