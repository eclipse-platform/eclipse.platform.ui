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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.text.DateFormat;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.ui.synchronize.viewers.SyncInfoModelElement;

public class ChangeLogDiffNode extends SyncInfoModelElement {

	private ILogEntry logEntry;

	public ChangeLogDiffNode(DiffNode parent, ILogEntry logEntry) {
		//super(parent, new SyncInfoTree(), ResourcesPlugin.getWorkspace().getRoot());
		super(null, null);
		this.logEntry = logEntry;
	}

	public ILogEntry getComment() {
		return logEntry;
	}
	
	public boolean equals(Object other) {
		if(other == this) return true;
		if(! (other instanceof ChangeLogDiffNode)) return false;
		return ((ChangeLogDiffNode)other).getComment().equals(getComment());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_DATE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		String date = DateFormat.getDateTimeInstance().format(logEntry.getDate());
		return date + ": " + logEntry.getComment() + " (" + logEntry.getAuthor() +")";
	}

	public void add(SyncInfo info) {
		//((SubscriberSyncInfoSet)getSyncInfoTree()).add(info);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SyncInfoModelElement#toString()
	 */
	public String toString() {
		return getLabel(null);
	}
}
