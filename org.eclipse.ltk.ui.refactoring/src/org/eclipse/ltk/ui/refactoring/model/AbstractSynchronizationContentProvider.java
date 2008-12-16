/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.model;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringDescriptorProxyAdapter;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryImplementation;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
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
 * provider used in team synchronization views may use this class as a basis for
 * refactoring-aware synchronization content providers.
 * </p>
 *
 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider
 *
 * @since 3.2
 */
public abstract class AbstractSynchronizationContentProvider extends SynchronizationContentProvider {

	/**
	 * Returns the refactorings for the specified project which are not in sync.
	 * <p>
	 * This method fetches refactoring information for all refactorings which
	 * are not in sync for a project (e.g. have not yet been checked into the
	 * repository, or are pending refactorings to execute on the local
	 * workspace).
	 * </p>
	 *
	 * @param context
	 *            the synchronization context to use
	 * @param project
	 *            the project to compute its refactorings
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code> if no
	 *            progress monitoring or cancelation is desired
	 * @return the refactoring history representing the refactorings
	 */
	protected RefactoringHistory getRefactorings(final ISynchronizationContext context, final IProject project, IProgressMonitor monitor) {
		Assert.isNotNull(context);
		Assert.isNotNull(project);
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			monitor.beginTask(RefactoringUIMessages.RefactoringModelMerger_retrieving_refactorings, IProgressMonitor.UNKNOWN);
			final IProgressMonitor finalMonitor= monitor;
			final Set result= new HashSet();
			final IResourceDiffTree tree= context.getDiffTree();
			tree.accept(project.getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER).getFullPath(), new IDiffVisitor() {

				public final boolean visit(final IDiff diff) {
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
									AbstractResourceMappingMerger.getRefactoringDescriptors(revision, localDescriptors, new SubProgressMonitor(finalMonitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
							}
						}
						final ITwoWayDiff remoteDiff= threeWay.getLocalChange();
						if (remoteDiff instanceof IResourceDiff && remoteDiff.getKind() != IDiff.NO_CHANGE) {
							final IResourceDiff resourceDiff= (IResourceDiff) remoteDiff;
							final IFileRevision revision= resourceDiff.getAfterState();
							if (revision != null) {
								final String name= revision.getName();
								if (name.equalsIgnoreCase(RefactoringHistoryService.NAME_HISTORY_FILE))
									AbstractResourceMappingMerger.getRefactoringDescriptors(revision, remoteDescriptors, new SubProgressMonitor(finalMonitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
							}
						}
						final Set local= new HashSet(localDescriptors);
						local.removeAll(remoteDescriptors);
						for (final Iterator iterator= local.iterator(); iterator.hasNext();) {
							final RefactoringDescriptor descriptor= (RefactoringDescriptor) iterator.next();
							result.add(new RefactoringDescriptorSynchronizationProxy(new RefactoringDescriptorProxyAdapter(descriptor), project.getName(), IThreeWayDiff.OUTGOING));
						}
						final Set remote= new HashSet(remoteDescriptors);
						remote.removeAll(localDescriptors);
						for (final Iterator iterator= remote.iterator(); iterator.hasNext();) {
							final RefactoringDescriptor descriptor= (RefactoringDescriptor) iterator.next();
							result.add(new RefactoringDescriptorSynchronizationProxy(new RefactoringDescriptorProxyAdapter(descriptor), project.getName(), IThreeWayDiff.INCOMING));
						}
					}
					return true;
				}
			}, IResource.DEPTH_INFINITE);

			for (final Iterator iterator= result.iterator(); iterator.hasNext();) {
				final RefactoringDescriptorSynchronizationProxy proxy= (RefactoringDescriptorSynchronizationProxy) iterator.next();
				if (!includeDirection(proxy.getDirection()))
					result.remove(proxy);
			}

			return new RefactoringHistoryImplementation((RefactoringDescriptorProxy[]) result.toArray(new RefactoringDescriptorProxy[result.size()]));
		} finally {
			monitor.done();
		}
	}
}
