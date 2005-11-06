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
package org.eclipse.ltk.internal.core.refactoring.history;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringPreferenceConstants;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistoryEvent;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * Refactoring history listener which continuosly persists the global
 * refactoring history.
 * 
 * @since 3.2
 */
public final class RefactoringHistorySerializer implements IRefactoringHistoryListener {

	/**
	 * {@inheritDoc}
	 */
	public void historyNotification(final RefactoringHistoryEvent event) {
		final boolean enabled= RefactoringCorePlugin.getDefault().getPluginPreferences().getBoolean(RefactoringPreferenceConstants.PREFERENCE_ENABLE_WORKSPACE_REFACTORING_HISTORY);
		if (enabled) {
			final RefactoringDescriptor descriptor= event.getDescriptor();
			if (!descriptor.isUnknown()) {
				final long stamp= descriptor.getTimeStamp();
				if (stamp >= 0) {
					final String name= descriptor.getProject();
					if (name != null && !"".equals(name)) { //$NON-NLS-1$
						final IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
						if (project.isAccessible()) {
							if (RefactoringHistoryService.isHistoryEnabled(project)) {
								final URI uri= project.getLocationURI();
								if (uri != null) {
									try {
										processHistoryNotification(EFS.getStore(uri).getChild(RefactoringHistoryService.NAME_REFACTORINGS_FOLDER), event, name);
									} catch (CoreException exception) {
										RefactoringCorePlugin.log(exception);
									} finally {
										try {
											project.refreshLocal(IResource.DEPTH_INFINITE, null);
										} catch (CoreException exception) {
											RefactoringCorePlugin.log(exception);
										}
									}
								}
							} else {
								try {
									processHistoryNotification(EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(RefactoringHistoryService.NAME_REFACTORINGS_FOLDER).getChild(name), event, name);
								} catch (CoreException exception) {
									RefactoringCorePlugin.log(exception);
								}
							}
						}
					} else {
						try {
							processHistoryNotification(EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(RefactoringHistoryService.NAME_REFACTORINGS_FOLDER).getChild(RefactoringHistoryService.NAME_WORKSPACE_PROJECT), event, name);
						} catch (CoreException exception) {
							RefactoringCorePlugin.log(exception);
						}
					}
				}
			}
		}
	}

	/**
	 * Processes the history event.
	 * 
	 * @param store
	 *            the file store
	 * @param event
	 *            the history event
	 * @param name
	 *            the project name, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs
	 */
	private void processHistoryNotification(final IFileStore store, final RefactoringHistoryEvent event, final String name) throws CoreException {
		Assert.isNotNull(store);
		Assert.isNotNull(event);
		final RefactoringDescriptor descriptor= event.getDescriptor();
		final int type= event.getEventType();
		final RefactoringHistoryManager manager= new RefactoringHistoryManager(store, name);
		if (type == RefactoringHistoryEvent.ADDED)
			manager.addDescriptor(descriptor);
		else if (type == RefactoringHistoryEvent.REMOVED)
			manager.removeDescriptor(descriptor.getTimeStamp());
	}
}