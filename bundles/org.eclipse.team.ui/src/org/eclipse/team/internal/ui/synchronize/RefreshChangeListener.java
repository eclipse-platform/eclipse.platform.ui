/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.util.*;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.subscribers.*;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.core.subscribers.SubscriberSyncInfoCollector;

public class RefreshChangeListener implements ISubscriberChangeListener {
	private List changes = new ArrayList();
	private SubscriberSyncInfoCollector collector;

	public RefreshChangeListener(SubscriberSyncInfoCollector collector) {
		this.collector = collector;
	}
	public void subscriberResourceChanged(ISubscriberChangeEvent[] deltas) {
		for (int i = 0; i < deltas.length; i++) {
			ISubscriberChangeEvent delta = deltas[i];
			if (delta.getFlags() == ISubscriberChangeEvent.SYNC_CHANGED) {
				changes.add(delta);
			}
		}
	}
	public SyncInfo[] getChanges() {
		collector.waitForCollector(new NullProgressMonitor());
		List changedSyncInfos = new ArrayList();
		SyncInfoSet set = collector.getSyncInfoSet();
		for (Iterator it = changes.iterator(); it.hasNext();) {
			ISubscriberChangeEvent delta = (ISubscriberChangeEvent) it.next();
			SyncInfo info = set.getSyncInfo(delta.getResource());
			if (info != null && interestingChange(info)) {			
				changedSyncInfos.add(info);
			}
		}
		return (SyncInfo[]) changedSyncInfos.toArray(new SyncInfo[changedSyncInfos.size()]);
	}

	private boolean interestingChange(SyncInfo info) {
		int kind = info.getKind();
		if(isThreeWay()) {
			int direction = SyncInfo.getDirection(kind);
			return (direction == SyncInfo.INCOMING || direction == SyncInfo.CONFLICTING);
		} else {
			return SyncInfo.getChange(kind) != SyncInfo.IN_SYNC;
		}
	}
	
	private boolean isThreeWay() {
		return collector.getSubscriber().getResourceComparator().isThreeWay();
	}
	
	public void clear() {
		changes.clear();
	}
}
