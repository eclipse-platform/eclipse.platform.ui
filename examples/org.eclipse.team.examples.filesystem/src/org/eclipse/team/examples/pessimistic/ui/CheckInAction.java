/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.team.examples.pessimistic.ui;
 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.examples.pessimistic.PessimisticFilesystemProvider;

/**
 * Performs a check in on the selected resources.  If a folder is 
 * selected all of its children are recursively checked in.
 */
public class CheckInAction extends SourceManagementAction {

	/**
	 * Answers <code>true</code> if and only if the resource is 
	 * not null, controlled, not ignored, and is checked out.
	 * 
	 * @see PessimisticProviderAction#shouldEnableFor(IResource)
	 */
	protected boolean shouldEnableFor(IResource resource) {
		if (resource == null)
			return false;
		PessimisticFilesystemProvider provider= getProvider(resource);
		if (provider == null)
			return false;
		if (!provider.isControlled(resource))
			return false;
		if (provider.isIgnored(resource))
			return false;			
		return provider.isCheckedout(resource);
	}

	/*
	 * @see SourceControlAction#manageResources(PessimisticFilesystemProvider, IResource[], IProgressMonitor)
	 */
	protected void manageResources(PessimisticFilesystemProvider provider, IResource[] resources, IProgressMonitor monitor) {
		provider.checkin(resources, monitor);
	}

}
