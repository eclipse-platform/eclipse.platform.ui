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
package org.eclipse.team.internal.ui.sync.sets;

/**
 * This interface is used to receive SyncSetChangedEvents from a sync set.
 */
public interface ISyncSetChangedListener {
	
	/**
	 * The sync set has changed and the event contains the details.
	 */
	public void syncSetChanged(SyncSetChangedEvent event);
}
