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
 * A SyncInfoFilter is used by a SyncSetInput to detemine which resources
 * should be part of the sync set.
 */
public class SyncInfoFilter {
	
	public static SyncInfoFilter getDirectionAndChangeFilter(int direction, int change) {
		return new AndSyncInfoFilter(new SyncInfoFilter[] {
			new SyncInfoDirectionFilter(direction),
			new SyncInfoChangeTypeFilter(change)
		});	
	}
	
	/**
	 * Return true if the provided SyncInfo matches the filter.
	 * The default behavior it to include resources whose syncKind
	 * is non-zero.
	 * 
	 * @param info
	 * @return
	 */
	public boolean select(SyncInfo info) {
		return info.getKind() != 0;
	}
}
