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
package org.eclipse.team.examples.filesystem;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.IResourceVariant;
import org.eclipse.team.core.synchronize.IResourceVariantComparator;
import org.eclipse.team.internal.core.subscribers.caches.SyncTreeSubscriber;


public class FileSystemSubscriber extends SyncTreeSubscriber {

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.SyncTreeSubscriber#getRemoteResource(org.eclipse.core.resources.IResource)
	 */
	public IResourceVariant getRemoteResource(IResource resource) throws TeamException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.SyncTreeSubscriber#getBaseResource(org.eclipse.core.resources.IResource)
	 */
	public IResourceVariant getBaseResource(IResource resource) throws TeamException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.SyncTreeSubscriber#hasRemote(org.eclipse.core.resources.IResource)
	 */
	protected boolean hasRemote(IResource resource) throws TeamException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.SyncTreeSubscriber#getResourceComparator()
	 */
	public IResourceVariantComparator getResourceComparator() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#isSupervised(org.eclipse.core.resources.IResource)
	 */
	public boolean isSupervised(IResource resource) throws TeamException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#roots()
	 */
	public IResource[] roots() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.Subscriber#refresh(org.eclipse.core.resources.IResource[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void refresh(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		// TODO Auto-generated method stub
		
	}	
}
