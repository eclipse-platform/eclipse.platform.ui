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
package org.eclipse.team.ui.sync;

import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.sync.IRemoteSyncElement;

/**
 * Thsi class filters the SyncInfo by change type (addition, deletion, change)
 */
public class SyncInfoChangeTypeFilter extends SyncInfoFilter {

	private int[] changeFilters = new int[] {IRemoteSyncElement.ADDITION, IRemoteSyncElement.DELETION, IRemoteSyncElement.CHANGE};

	public SyncInfoChangeTypeFilter(int[] changeFilters) {
		this.changeFilters = changeFilters;
	}
	
	public SyncInfoChangeTypeFilter(int change) {
		this(new int[] { change });
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.views.SyncSetFilter#select(org.eclipse.team.core.sync.SyncInfo)
	 */
	public boolean select(SyncInfo info) {
		int syncKind = info.getKind();
		for (int i = 0; i < changeFilters.length; i++) {
			int filter = changeFilters[i];
			if ((syncKind & SyncInfo.CHANGE_MASK) == filter)
				return true;
		}
		return false;
	}
	
}
