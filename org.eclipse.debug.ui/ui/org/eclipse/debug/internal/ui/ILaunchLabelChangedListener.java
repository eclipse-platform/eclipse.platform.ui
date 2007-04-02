/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

import org.eclipse.debug.ui.ILaunchGroup;

/**
 * Implementors of this interface are notified when a changed has been made to 
 * the cache of context sensitive labels stored in the <code>LaunchingResourceManager</code>.
 * A change can come from either a selection change, or from a change to the launch history(s).
 * 
 * <p>
 * Clients are intended to implement this interface
 * </p>
 * 
 * @since 3.3
 */
public interface ILaunchLabelChangedListener {
	
	/**
	 * This method is called back to by <code>LaunchingResourceManager</code> iff the cache of labels
	 * for the current set of listeners has changed
	 */
	public void labelChanged();
	
	/**
	 * Returns the launch group that this listener is associated with.
	 * @return the launch group that this listener is associated with, or <code>null</code> if none
	 */
	public ILaunchGroup getLaunchGroup();
}
