/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.filesystem.ui;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.examples.filesystem.FileSystemPlugin;
import org.eclipse.team.internal.ui.actions.TeamAction;

/**
 * An abstract class that acts as a super class for FileSystemProvider actions.
 * It provides some general methods applicable to multipe actions.
 */
public abstract class FileSystemAction extends TeamAction {

	/**
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			// we only want to work on resources mapped to a file system provider
			RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), FileSystemPlugin.PROVIDER_ID);
			if (provider == null)
				return false;
		}
		return true;
	}

	/**
	 * Split the resources into sets associated with their project/provider
	 */
	protected Map getRepositoryProviderMapping() {
		HashMap result = new HashMap();
		IResource[] resources = getSelectedResources();
		for (int i = 0; i < resources.length; i++) {
			RepositoryProvider provider = RepositoryProvider.getProvider(resources[i].getProject());
			List list = (List) result.get(provider);
			if (list == null) {
				list = new ArrayList();
				result.put(provider, list);
			}
			list.add(resources[i]);
		}
		return result;
	}
	
}
