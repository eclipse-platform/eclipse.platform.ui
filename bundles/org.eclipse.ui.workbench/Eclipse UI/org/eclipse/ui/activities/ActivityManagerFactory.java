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

import org.eclipse.ui.internal.activities.MutableActivityManager;

/**
 * <p>
 * This class allows clients to broker instances of <code>IActivityManager</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IActivityManager
 */
public final class ActivityManagerFactory {

	/**
	 * Creates a new instance of IMutableActivityManager.
	 * 
	 * @return a new instance of IMutableActivityManager. Clients should not
	 *         make assumptions about the concrete implementation outside the
	 *         contract of <code>IMutableActivityManager</code>. Guaranteed
	 *         not to be <code>null</code>.
	 */
	public static IMutableActivityManager getMutableActivityManager() {
		return new MutableActivityManager();
	}

	private ActivityManagerFactory() {
	}
}
