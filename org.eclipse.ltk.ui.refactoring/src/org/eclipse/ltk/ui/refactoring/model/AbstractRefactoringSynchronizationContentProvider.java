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
import java.util.Set;

import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.diff.IDiffVisitor;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.diff.ITwoWayDiff;
import org.eclipse.team.core.history.IFileState;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.ui.mapping.SynchronizationContentProvider;

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

import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.IExtensionStateModel;

/**
 * Partial implementation of a refactoring-aware synchronization content
 * provider.
 * <p>
 * Note: this class is intended to be extended by clients who need refactoring
 * support in a synchronization content provider.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider
 * 
 * @since 3.2
 */
public abstract class AbstractRefactoringSynchronizationContentProvider extends SynchronizationContentProvider {

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		super.dispose();
		final ISynchronizationContext context= getContext();
		if (context != null)
			context.getDiffTree().removeDiffChangeListener(this);
	}

	/**
	 * Returns the incoming refactoring history for the specified project.
	 * <p>
	 * This method fetches all refactorings which are present in the remote
	 * location and have not already been performed on the local workspace.
	 * </p>
	 * 
	 * @param context
	 *            the synchronization context
	 * @param project
	 *            the project to get its incoming refactorings
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @return the refactoring history representing the incoming refactorings
	 */
	protected RefactoringHistory getIncomingRefactorings(final ISynchronizationContext context, final IProject project, IProgressMonitor monitor) {
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			monitor.beginTask(RefactoringUIMessages.RefactoringModelMerger_retrieving_refactorings, 12);
			final IProgressMonitor finalMonitor= monitor;
			final Set incoming= new HashSet();
			try {
				final IResourceDiffTree tree= context.getDiffTree();
				tree.accept(project.getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER).getFullPath(), new IDiffVisitor() {

					public final boolean visit(final IDiffNode node) throws CoreException {
						if (node instanceof IThreeWayDiff) {
							final IThreeWayDiff threeWay= (IThreeWayDiff) node;
							final IResource resource= tree.getResource(node);
							if (resource.getName().equals(RefactoringHistoryService.NAME_HISTORY_FILE) && resource.getType() == IResource.FILE) {
								final int direction= threeWay.getDirection();
								if (direction == IThreeWayDiff.INCOMING || direction == IThreeWayDiff.CONFLICTING) {
									final ITwoWayDiff twoWay= threeWay.getRemoteChange();
									if (twoWay instanceof IResourceDiff) {
										final IFileState remoteState= ((IResourceDiff) twoWay).getAfterState();
										IStorage storage= null;
										try {
											storage= remoteState.getStorage(new SubProgressMonitor(finalMonitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
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
													for (int offset= 0; offset < proxies.length; offset++)
														incoming.add(proxies[offset]);
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
								}
							}
						}
						return true;
					}
				}, IResource.DEPTH_INFINITE);
			} catch (CoreException exception) {
				RefactoringUIPlugin.log(exception);
			}
			final RefactoringHistory history= RefactoringCore.getRefactoringHistoryService().getProjectHistory(project, new SubProgressMonitor(monitor, 2, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
			final RefactoringDescriptorProxy[] descriptors= history.getDescriptors();
			for (int index= 0; index < descriptors.length; index++)
				incoming.remove(descriptors[index]);
			final RefactoringDescriptorProxy[] proxies= new RefactoringDescriptorProxy[incoming.size()];
			incoming.toArray(proxies);
			return new RefactoringHistoryImplementation(proxies);
		} finally {
			monitor.done();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(final IExtensionStateModel model, final IMemento memento) {
		super.init(model, memento);
		final ISynchronizationContext context= getContext();
		if (context != null)
			context.getDiffTree().addDiffChangeListener(this);
	}
}