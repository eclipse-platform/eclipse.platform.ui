/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.pessimistic;
 
import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ResourceSetContentProvider implements ITreeContentProvider {
	private static final Object[] EMPTY_ARRAY= new Object[0];
	private Map fResourceTree;
	private IResource[] fRoots;

	public ResourceSetContentProvider(Set resources) {
		fResourceTree= new HashMap(1);
		Set roots= new HashSet(resources);
		for(Iterator i= resources.iterator(); i.hasNext(); ) {
			IResource resource= (IResource)i.next();
			if(resource.getType() == IResource.ROOT) {
				continue; // root cannot be displayed
			}
			IResource parent= resource.getParent();
			if (roots.contains(parent)) {
				roots.remove(resource);
				Set set= (Set)fResourceTree.get(parent);
				if (set == null) {
					set= new HashSet(1);
					fResourceTree.put(parent, set);
				}
				set.add(resource);
			}
		}
		fRoots= (IResource[])roots.toArray(new IResource[roots.size()]);
	}

	public Object[] getChildren(Object parentElement) {
		Set set= (Set) fResourceTree.get(parentElement);
		if (set != null) {
			return set.toArray();
		}
		return EMPTY_ARRAY;
	}

	public Object getParent(Object element) {
		if (element instanceof IResource) {
			return ((IResource)element).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		return fResourceTree.get(element) != null;
	}

	public Object[] getElements(Object inputElement) {
		return fRoots;
	}

	public void dispose() {
		fResourceTree= null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
