/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.TeamHook;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.RepositoryProvider;

/**
 * This class forwards TeamHook callbacks to the proper RepositoryProvider
 */
public class TeamHookDispatcher extends TeamHook {

	/**
	 * @see org.eclipse.core.resources.team.TeamHook#validateCreateLink(org.eclipse.core.resources.IFile, int, org.eclipse.core.runtime.IPath)
	 */
	public IStatus validateCreateLink(IFile file, int updateFlags, IPath location) {
		RepositoryProvider provider = getProvider(file);
		if (provider == null) {
			return super.validateCreateLink(file, updateFlags, location);
		} else {
			return provider.validateCreateLink(file, updateFlags, location);
		}
	}

	/**
	 * @see org.eclipse.core.resources.team.TeamHook#validateCreateLink(org.eclipse.core.resources.IFolder, int, org.eclipse.core.runtime.IPath)
	 */
	public IStatus validateCreateLink(IFolder folder, int updateFlags, IPath location) {
		RepositoryProvider provider = getProvider(folder);
		if (provider == null) {
			return super.validateCreateLink(folder, updateFlags, location);
		} else {
			return provider.validateCreateLink(folder, updateFlags, location);
		}
	}
	
	/**
	 * Method getProvider.
	 * @param folder
	 * @return RepositoryProvider
	 */
	private RepositoryProvider getProvider(IResource resource) {
		return RepositoryProvider.getProvider(resource.getProject());
	}

	
}
