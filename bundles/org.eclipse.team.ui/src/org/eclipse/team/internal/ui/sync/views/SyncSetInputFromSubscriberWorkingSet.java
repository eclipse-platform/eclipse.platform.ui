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
package org.eclipse.team.internal.ui.sync.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.IWorkingSet;

/**
 * Thsi class filters the sync set input using a working set
 */
public class SyncSetInputFromSubscriberWorkingSet extends SyncSetInputFromSubscriber {

	private IWorkingSet workingSet;

	public SyncSetInputFromSubscriberWorkingSet(TeamSubscriber subscriber) {
		setSubscriber(subscriber);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.views.SyncSetInputFromSubscriber#collect(org.eclipse.core.resources.IResource, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void collect(IResource resource, IProgressMonitor monitor) throws TeamException {
		// Only collect the change for the resource if the resource is in the working set
		if (isIncluded(resource)) {
			super.collect(resource, monitor);
		}
	}

	/*
	 * Answer true if the given resource is included in the working set
	 */
	private boolean isIncluded(IResource resource) {
		// if there's no set, the resource is included
		if (workingSet == null) return true;
		// otherwise, if their is a parent of the resource in the set,
		// it is included
		Object[] elements = workingSet.getElements();
		List result = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			IResource setResource = getResource(elements[i]);
			if (isParent(setResource, resource)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.views.SyncSetInputFromSubscriber#getRoots()
	 */
	protected IResource[] getRoots() {
		IResource[] roots = super.getRoots();
		if (workingSet == null) return roots;
		
		// filter the roots by the selected working set
		Set result = new HashSet();
		for (int i = 0; i < roots.length; i++) {
			IResource resource = roots[i];
			result.addAll(Arrays.asList(getIntersectionWithSet(resource)));
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
	}

	/*
	 * Answer the intersection between the given resource and it's children
	 * and the receiver's working set.
	 */
	private IResource[] getIntersectionWithSet(IResource resource) {
		Object[] elements = workingSet.getElements();
		List result = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			IResource setResource = getResource(elements[i]);
			if (setResource != null) {
				if (isParent(resource, setResource)) {
					try {
						if (getSubscriber().isSupervised(setResource)) {
							result.add(setResource);
						}
					} catch (TeamException e) {
						// Log the exception and add the resource to the list
						TeamUIPlugin.log(e);
						result.add(setResource);
					}
				} else if (isParent(setResource, resource)) {
					result.add(resource);
				}
			}
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
	}

	private boolean isParent(IResource parent, IResource child) {
		return (parent.getFullPath().isPrefixOf(child.getFullPath()));
	}

	private IResource getResource(Object object) {
		if (object instanceof IResource) {
			return (IResource)object;
		} else if (object instanceof IAdaptable) {
			return (IResource)((IAdaptable)object).getAdapter(IResource.class);
		}
		return null;
	}

	public IWorkingSet getWorkingSet() {
		return workingSet;
	}

	public void setWorkingSet(IWorkingSet set) {
		this.workingSet = set;
	}

}
