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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * This class sorts the model elements that appear in the SynchronizeView
 */
public class SyncViewerSorter extends ResourceSorter {
			
	private boolean compareFullPaths = false;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.navigator.ResourceSorter#compareNames(org.eclipse.core.resources.IResource, org.eclipse.core.resources.IResource)
	 */
	protected int compareNames(IResource resource1, IResource resource2) {
		if(compareFullPaths) {
			return collator.compare(resource1.getFullPath().toString(), resource2.getFullPath().toString());
		} else {
			return collator.compare(resource1.getName(), resource2.getName());
		}
	}

	public SyncViewerSorter(int criteria) {
		super(criteria);
	}

	/* (non-Javadoc)
	 * Method declared on ViewerSorter.
	 */
	public int compare(Viewer viewer, Object o1, Object o2) {
		if(o1 instanceof CompressedFolder || o2 instanceof CompressedFolder) {
			compareFullPaths = true;
		}
		int result = super.compare(viewer, getResource(o1), getResource(o2));
		compareFullPaths = false;
		return result;
	}
	
	protected IResource getResource(Object obj) {
		return (IResource)TeamAction.getAdapter(obj, IResource.class);
	}
}
