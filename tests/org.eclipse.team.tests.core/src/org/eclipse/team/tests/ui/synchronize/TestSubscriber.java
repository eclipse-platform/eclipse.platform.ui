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
package org.eclipse.team.tests.ui.synchronize;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.*;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestSubscriber extends TeamSubscriber {
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.TeamSubscriber#getDescription()
	 */
	public String getDescription() {
		return "Test Subscriber";
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.TeamSubscriber#isSupervised(org.eclipse.core.resources.IResource)
	 */
	public boolean isSupervised(IResource resource) throws TeamException {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.TeamSubscriber#members(org.eclipse.core.resources.IResource)
	 */
	public IResource[] members(IResource resource) throws TeamException {
		if (resource.getType() == IResource.FILE) {
			return new IResource[0];
		}
		try {
			return ((IContainer)resource).members();
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.TeamSubscriber#roots()
	 */
	public IResource[] roots() {
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.TeamSubscriber#getSyncInfo(org.eclipse.core.resources.IResource)
	 */
	public SyncInfo getSyncInfo(IResource resource) throws TeamException {
		return  getSyncInfo(resource, SyncInfo.IN_SYNC);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.TeamSubscriber#refresh(org.eclipse.core.resources.IResource[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void refresh(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		// Nothing to do
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.TeamSubscriber#getDefaultComparisonCriteria()
	 */
	public IComparisonCriteria getDefaultComparisonCriteria() {
		return new IComparisonCriteria() {
			public boolean compare(Object e1, Object e2) {
				return false;
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.TeamSubscriber#isThreeWay()
	 */
	public boolean isThreeWay() {
		return false;
	}
	
	public SyncInfo getSyncInfo(IResource resource, int i) throws TeamException {
		return new TestSyncInfo(resource, i, this);
	}
}
