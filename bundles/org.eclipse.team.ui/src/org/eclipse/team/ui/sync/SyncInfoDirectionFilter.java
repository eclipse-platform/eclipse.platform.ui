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
 * Filter the SyncInfo by a set of directions (incoming, outgoing, conflict)
 */
public class SyncInfoDirectionFilter extends SyncInfoFilter {

	int[] directionFilters = new int[] {SyncInfo.OUTGOING, SyncInfo.INCOMING, SyncInfo.CONFLICTING};

	public SyncInfoDirectionFilter(int[] directionFilters) {
		this.directionFilters = directionFilters;
	}
	
	public SyncInfoDirectionFilter(int direction) {
		this(new int[] { direction });
	}

	/* (non-Javadoc)
	 * @see SyncSetFilter#select(org.eclipse.team.core.sync.SyncInfo)
	 */
	public boolean select(SyncInfo info) {
		int syncKind = info.getKind();
		for (int i = 0; i < directionFilters.length; i++) {
			int filter = directionFilters[i];
			if ((syncKind & SyncInfo.DIRECTION_MASK) == filter)
				return true;
		}
		return false;
	}
}
