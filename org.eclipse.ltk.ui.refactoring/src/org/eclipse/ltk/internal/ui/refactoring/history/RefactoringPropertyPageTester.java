/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import java.net.URI;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

/**
 * Property tester for the 'refactoringPropertiesEnabled' property.
 *
 * @since 3.3
 */
public final class RefactoringPropertyPageTester extends PropertyTester {

	/** The property name */
	public static final String PROPERTY_NAME= "refactoringPropertiesEnabled"; //$NON-NLS-1$

	private IFileStore getHistoryStore(final IProject project) {
		final IPath location= RefactoringCorePlugin.getDefault().getStateLocation();
		final IFileStore store= EFS.getLocalFileSystem().getStore(location).getChild(RefactoringHistoryService.NAME_HISTORY_FOLDER);
		try {
			if (project.isAccessible()) {
				if (RefactoringHistoryService.hasSharedRefactoringHistory(project)) {
					final URI uri= project.getLocationURI();
					if (uri != null)
						return EFS.getStore(uri).getChild(RefactoringHistoryService.NAME_HISTORY_FOLDER);
				} else
					return store.getChild(project.getName());
			}
		} catch (CoreException exception) {
			// Do nothing
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean test(final Object receiver, final String property, final Object[] arguments, final Object expected) {
		if (PROPERTY_NAME.equals(property)) {
			if (receiver instanceof IAdaptable) {
				final IAdaptable adaptable= (IAdaptable) receiver;
				final IResource resource= (IResource) adaptable.getAdapter(IResource.class);
				if (resource instanceof IProject) {
					final IProject project= (IProject) resource;
					final IFileStore store= getHistoryStore(project);
					if (store != null)
						return store.fetchInfo().exists();
				}
			}
		}
		return false;
	}
}