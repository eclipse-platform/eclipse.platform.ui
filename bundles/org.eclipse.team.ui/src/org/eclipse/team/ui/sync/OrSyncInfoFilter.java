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

/**
 * Selects SyncInfo that match any of the child filters.
 */
public class OrSyncInfoFilter extends CompoundSyncInfoFilter {
	public OrSyncInfoFilter(SyncInfoFilter[] filters) {
		super(filters);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.views.SyncSetFilter#select(org.eclipse.team.core.sync.SyncInfo)
	 */
	public boolean select(SyncInfo info) {
		for (int i = 0; i < filters.length; i++) {
			SyncInfoFilter filter = filters[i];
			if (filter.select(info)) {
				return true;
			}
		}
		return false;
	}
}
