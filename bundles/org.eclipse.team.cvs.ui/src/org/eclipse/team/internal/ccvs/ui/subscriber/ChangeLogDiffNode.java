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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ui.synchronize.SynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;

/**
 * A model element corresponding to a "commit set". It displays a CVS
 * log entry using a currently fixed format. A commit doesn't have an
 * associated resource.
 * 
 * @since 3.0
 */
public class ChangeLogDiffNode extends SynchronizeModelElement {

	private ILogEntry logEntry;

	public ChangeLogDiffNode(ISynchronizeModelElement parent, ILogEntry logEntry) {
		super(parent);
		this.logEntry = logEntry;
	}

	public ILogEntry getComment() {
		return logEntry;
	}
	
	public boolean equals(Object obj) {
		return (obj == this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_CHANGELOG);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#getName()
	 */
	public String getName() {
		String date = DateFormat.getDateTimeInstance().format(logEntry.getDate());
		String comment = HistoryView.flattenText(logEntry.getComment());
		return "["+logEntry.getAuthor()+ "] (" + date +") " + comment; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SyncInfoModelElement#toString()
	 */
	public String toString() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelElement#getResource()
	 */
	public IResource getResource() {
		return null;
	}

    public String getShortName() {
		String date = DateFormat.getDateTimeInstance().format(logEntry.getDate());
		return "["+logEntry.getAuthor()+ "] (" + date +")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
