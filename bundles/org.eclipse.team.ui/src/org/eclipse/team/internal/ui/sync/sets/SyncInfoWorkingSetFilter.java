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
package org.eclipse.team.internal.ui.sync.sets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.sync.SyncInfoFilter;
import org.eclipse.ui.IWorkingSet;

/**
 * WorkingSet filter for a SyncSet.
 */
public class SyncInfoWorkingSetFilter extends SyncInfoFilter {

	private IWorkingSet workingSet;
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.SyncInfoFilter#select(org.eclipse.team.core.subscribers.SyncInfo)
	 */
	public boolean select(SyncInfo info) {
		// if there's no set, the resource is included
		if (workingSet == null) return true;
		return isIncluded(info.getLocal());
	}

	/*
	 * Answer true if the given resource is included in the working set
	 */
	private boolean isIncluded(IResource resource) {
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

	private IResource getResource(Object object) {
		if (object instanceof IResource) {
			return (IResource)object;
		} else if (object instanceof IAdaptable) {
			return (IResource)((IAdaptable)object).getAdapter(IResource.class);
		}
		return null;
	}

	private boolean isParent(IResource parent, IResource child) {
		return (parent.getFullPath().isPrefixOf(child.getFullPath()));
	}

	public IWorkingSet getWorkingSet() {
		return workingSet;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.views.SyncSetInputFromSubscriber#getRoots()
	 */
	public IResource[] getRoots(TeamSubscriber subscriber) {
		IResource[] roots = subscriber.roots();
		if (workingSet == null) return roots;
		
		// filter the roots by the selected working set
		Set result = new HashSet();
		for (int i = 0; i < roots.length; i++) {
			IResource resource = roots[i];
			result.addAll(Arrays.asList(getIntersectionWithSet(subscriber, resource)));
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
	}

	/*
	 * Answer the intersection between the given resource and it's children
	 * and the receiver's working set.
	 */
	private IResource[] getIntersectionWithSet(TeamSubscriber subscriber, IResource resource) {
		Object[] elements = workingSet.getElements();
		List result = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			IResource setResource = getResource(elements[i]);
			if (setResource != null) {
				if (isParent(resource, setResource)) {
					try {
						if (subscriber.isSupervised(setResource)) {
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
	/**
	 * @param workingSet
	 */
	public void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;
	}

}
