/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import org.eclipse.ui.activities.IMutableActivityManager;

/**
 * This class allows clients to broker instances of <code>IActivityManager</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 */
public final class ActivityManagerFactory {

	/**
	 * Creates a new instance of <code>IMutableActivityManager</code>.
	 * 
	 * @return a new instance of <code>IMutableActivityManager</code>.
	 *         Clients should not make assumptions about the concrete
	 *         implementation outside the contract of the interface. Guaranteed
	 *         not to be <code>null</code>.
	 */
	public static IMutableActivityManager getMutableActivityManager() {
		return new MutableActivityManager();
	}

	private ActivityManagerFactory() {
	}
}
