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

import java.util.Set;

/**
 * <p>
 * An instance of <code>IMutableActivityManager</code> can be used to obtain
 * instances of <code>IActivity</code> and <code>ICategory</code>, as well
 * as manage whether or not those instances are enabled or disabled.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see ActivityManagerFactory
 * @see IActivity
 * @see IActivityManagerListener
 * @see ICategory
 */
public interface IMutableActivityManager extends IActivityManager {

	/**
	 * Sets the set of identifiers to enabled activities.
	 * 
	 * @param enabledActivityIds
	 *            the set of identifiers to enabled activities. This set may be
	 *            empty, but it must not be <code>null</code>. If this set
	 *            is not empty, it must only contain instances of <code>String</code>.
	 */
	void setEnabledActivityIds(Set enabledActivityIds);
}
