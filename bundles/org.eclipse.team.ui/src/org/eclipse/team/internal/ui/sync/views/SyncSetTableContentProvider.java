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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.sync.sets.*;

/**
 * This class provides the contents for a TableViewer using a SyncSet as the model
 */
public class SyncSetTableContentProvider extends SyncSetContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object element) {
		SyncInfo[] infos = getSyncSet().allMembers();
		return getModelObjects(infos);
	}

	public TableViewer getTableViewer() {
		if (viewer instanceof TableViewer) {
			return (TableViewer)viewer;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.views.SyncSetContentProvider#handleResourceAdditions(org.eclipse.team.ccvs.syncviews.views.SyncSetChangedEvent)
	 */
	protected void handleResourceAdditions(SyncSetChangedEvent event) {
		TableViewer table = getTableViewer();
		if (table != null) {
			SyncInfo[] infos = event.getAddedResources();
			table.add(getModelObjects(infos));
		} else {
			super.handleResourceAdditions(event);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.views.SyncSetContentProvider#handleResourceRemovals(org.eclipse.team.ccvs.syncviews.views.SyncSetChangedEvent)
	 */
	protected void handleResourceRemovals(SyncSetChangedEvent event) {
		TableViewer table = getTableViewer();
		if (table != null) {
			IResource[] resources = event.getRemovedResources();
			table.remove(getModelObjects(resources));
		} else {
			super.handleResourceRemovals(event);
		}
	}

	protected Object getModelObject(SyncInfo info) {
		return getModelObject(info.getLocal());
	}
	
	protected Object[] getModelObjects(SyncInfo[] infos) {
		Object[] resources = new Object[infos.length];
		for (int i = 0; i < resources.length; i++) {
			resources[i] = getModelObject(infos[i]);
		}
		return resources;
	}
}
