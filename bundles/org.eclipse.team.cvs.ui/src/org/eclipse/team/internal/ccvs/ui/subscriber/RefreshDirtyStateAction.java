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

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.*;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Resets the dirty state of files whose contents match their base.
 */
public class RefreshDirtyStateAction extends SynchronizeModelAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.SubscriberAction#getSyncInfoFilter()
	 */
	protected FastSyncInfoFilter getSyncInfoFilter() {
		// Only interested in outgoing changes
		return new AndSyncInfoFilter(new FastSyncInfoFilter[] {
			new SyncInfoDirectionFilter(new int[] {SyncInfo.OUTGOING, SyncInfo.CONFLICTING }),
			new SyncInfoChangeTypeFilter(SyncInfo.CHANGE)
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SynchronizeModelAction#getSubscriberOperation(org.eclipse.ui.IWorkbenchPart, org.eclipse.compare.structuremergeviewer.IDiffElement[])
	 */
	protected SynchronizeModelOperation getSubscriberOperation(
			IWorkbenchPart part, IDiffElement[] elements) {
		return new RefreshDirtyStateOperation(part, elements);
	}
}
