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
package org.eclipse.team.tests.ccvs.core.subscriber;

import junit.framework.AssertionFailedError;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;

/**
 * This class acts as the source for the sync info used by the subscriber tests.
 * The purpose is to allow the sync info to be obtained directly from the subscriber 
 * or through the sync set visible in the sync view.
 */
public class SyncInfoSource {

	protected static IProgressMonitor DEFAULT_MONITOR = new NullProgressMonitor();

	/**
	 * Return the sync info for the given subscriber for the given resource.
	 */
	public SyncInfo getSyncInfo(TeamSubscriber subscriber, IResource resource) throws TeamException {
		return subscriber.getSyncInfo(resource, DEFAULT_MONITOR = new NullProgressMonitor());
	}
	
	/**
	 * Refresh the subscriber for the given resource
	 */
	public void refresh(TeamSubscriber subscriber, IResource resource) throws TeamException {
		subscriber.refresh(new IResource[] { resource}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);
	}
	
	protected void assertProjectRemoved(TeamSubscriber subscriber, IProject project) throws TeamException {
		IResource[] roots = subscriber.roots();
		for (int i = 0; i < roots.length; i++) {
			IResource resource = roots[i];
			if (resource.equals(project)) {
				throw new AssertionFailedError();
			}
		}
	}
	
}
