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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.OrSyncInfoFilter;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.SyncInfoDirectionFilter;
import org.eclipse.team.ui.synchronize.viewers.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.viewers.SynchronizeModelOperation;
import org.eclipse.ui.IWorkbenchPart;


public class MergeUpdateAction extends SynchronizeModelAction {
	
	private boolean promptBeforeUpdate;

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.SubscriberAction#getSyncInfoFilter()
	 */
	protected FastSyncInfoFilter getSyncInfoFilter() {
		// Update works for all incoming and conflicting nodes
		return new OrSyncInfoFilter(new FastSyncInfoFilter[] {
			new SyncInfoDirectionFilter(SyncInfo.INCOMING),
			new SyncInfoDirectionFilter(SyncInfo.CONFLICTING)
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.SubscriberAction#getSubscriberOperation(org.eclipse.compare.structuremergeviewer.IDiffElement[])
	 */
	protected SynchronizeModelOperation getSubscriberOperation(IWorkbenchPart part, IDiffElement[] elements) {
		return new MergeUpdateOperation(part, elements, promptBeforeUpdate);
	}

	public void setPromptBeforeUpdate(boolean prompt) {
		promptBeforeUpdate = prompt;
	}
}