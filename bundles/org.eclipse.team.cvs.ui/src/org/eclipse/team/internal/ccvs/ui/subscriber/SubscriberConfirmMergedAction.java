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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSSyncInfo;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.sync.SyncInfoDirectionFilter;
import org.eclipse.team.ui.sync.SyncInfoFilter;
import org.eclipse.team.ui.sync.SyncInfoSet;

/**
 * This action marks the local resource as merged by updating the base
 * resource revision to match the remote resource revision
 */
public class SubscriberConfirmMergedAction extends CVSSubscriberAction {

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.SubscriberAction#getSyncInfoFilter()
	 */
	protected SyncInfoFilter getSyncInfoFilter() {
		return new SyncInfoDirectionFilter(new int[] {SyncInfo.CONFLICTING});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberAction#run(org.eclipse.team.ui.sync.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void run(SyncInfoSet syncSet, IProgressMonitor monitor) throws CVSException {
		SyncInfo[] syncResources = syncSet.getSyncInfos();
		List needsMerge = new ArrayList();
		monitor.beginTask(null, 100 * syncResources.length);
		try {
			for (int i = 0; i < syncResources.length; i++) {
				SyncInfo resource = syncResources[i];
				
					CVSSyncInfo cvsInfo = getCVSSyncInfo(resource);
					if (cvsInfo != null) {
						cvsInfo.makeOutgoing(Policy.subMonitorFor(monitor, 100));
					}
	
			}
		} catch (TeamException e) {
			handle(e);
		} finally {
			monitor.done();
		}
	}


}
