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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.core.filesystem.EFS;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringPreferenceConstants;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryListener;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * Refactoring history listener which maintains project refactoring histories.
 * 
 * @since 3.2
 */
public final class ProjectRefactoringHistoryListener implements IRefactoringHistoryListener {

	/**
	 * {@inheritDoc}
	 */
	public void connect() {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public void descriptorAdded(final RefactoringDescriptor descriptor) throws CoreException {
		Assert.isNotNull(descriptor);
		final String name= descriptor.getProject();
		if (name != null && !"".equals(name)) { //$NON-NLS-1$
			final IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (project != null && project.exists() && isHistoryEnabled(project)) {
				final URI uri= project.getLocationURI();
				if (uri != null)
					new RefactoringHistoryManager(EFS.getStore(uri).getChild(RefactoringHistoryService.NAME_REFACTORINGS_FOLDER).toURI()).addDescriptor(descriptor);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void descriptorRemoved(final RefactoringDescriptor descriptor) throws CoreException {
		Assert.isNotNull(descriptor);
		final long stamp= descriptor.getTimeStamp();
		if (stamp >= 0) {
			final String name= descriptor.getProject();
			if (name != null && !"".equals(name)) { //$NON-NLS-1$
				final IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				if (project != null && project.exists() && isHistoryEnabled(project)) {
					final URI uri= project.getLocationURI();
					if (uri != null)
						new RefactoringHistoryManager(EFS.getStore(uri).getChild(RefactoringHistoryService.NAME_REFACTORINGS_FOLDER).toURI()).removeDescriptor(stamp);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void disconnect() {
		// Do nothing
	}

	/**
	 * Should a project refactoring history be maintained?
	 * 
	 * @param project
	 *            the project
	 * @return <code>true</code> if a history should be maintained,
	 *         <code>false</code> otherwise
	 */
	private boolean isHistoryEnabled(final IProject project) {
		Assert.isNotNull(project);
		final IScopeContext[] contexts= new IScopeContext[] { new ProjectScope(project)};
		final String preference= Platform.getPreferencesService().getString(RefactoringCorePlugin.getPluginId(), RefactoringPreferenceConstants.PREFERENCE_ENABLE_PROJECT_REFACTORING_HISTORY, Boolean.FALSE.toString(), contexts);
		if (preference != null)
			return Boolean.valueOf(preference).booleanValue();
		return false;
	}
}
