/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.activities;

/**
 * <p>
 * An instance of <code>IActivityManagerEvent</code> describes changes to an 
 * instance of <code>IActivityManager</code>. 
 * </p>
 * <p>
 * An instance of <code>IActivityManagerEvent</code> specifies the instance of 
 * <code>IActivityManager</code> that changed, but otherwise does not specify 
 * the nature of that change. Clients can only assume that one or more 
 * attributes of the particular instance of <code>IActivityManager</code> have 
 * changed. In the future, this interface may be extended to be more specific 
 * about the nature of the change.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IActivityManager
 * @see IActivityManagerListener#activityManagerChanged
 */
public interface IActivityManagerEvent {

	/**
	 * Returns the instance of <code>IActivityManager</code> that has changed.
	 *
	 * @return the instance of <code>IActivityManager</code> that has changed. 
	 *         Guaranteed not to be <code>null</code>.
	 */
	IActivityManager getActivityManager();
}
