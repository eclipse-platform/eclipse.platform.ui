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

package org.eclipse.ui.internal.csm.activities.api;

import org.eclipse.ui.internal.csm.activities.ActivityManager;

/**
 * <p>
 * This class allows clients to broker instances of 
 * <code>IActivityManager</code>.
 * </p>
 * <p>
 * Clients are not permitted to extend this class.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class ActivityManagerFactory {

	/**
	 * Creates a new instance of IActivityManager.
	 * 
	 * @return a new instance of IActivityManager. Clients should not make 
	 *         assumptions about the concrete implementation outside the 
	 *         contract of <code>IActivityManager</code>. Guaranteed not to be 
	 *         <code>null</code>.
	 */
	public static IActivityManager getActivityManager() {
		return new ActivityManager();
	}

	/**
	 * Private constructor to ensure that <code>ActivityManagerFactory</code> 
	 * can not be instantiated. 
	 */	
	private ActivityManagerFactory() {		
	}
}
