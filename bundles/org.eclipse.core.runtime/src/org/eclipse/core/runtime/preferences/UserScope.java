/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.preferences;

import java.net.URL;
import org.eclipse.core.internal.preferences.AbstractScope;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Object representing the user scope in the Eclipse preferences
 * hierarchy. Can be used as a context for searching for preference
 * values (in the IPreferencesService APIs) or for determining the 
 * correct preference node to set values in the store.
 * <p>
 * User preferences are stored on a per user basis in the
 * platform's user area as specified by <code>IPlatform#getUserLocation</code>.
 * </p>
 * <p>
 * The path for preferences defined in the user scope hierarchy
 * is as follows: <code>/user/&lt;qualifier&gt;</code>
 * </p>
 * @see IPlatform#getUserLocation()
 * @since 3.0
 */
public class UserScope extends AbstractScope {

	/**
	 * String constant (value of <code>"user"</code>) used for the 
	 * scope name for the user preference scope.
	 */
	public static final String SCOPE = "user"; //$NON-NLS-1$

	/*
	 * @see org.eclipse.core.runtime.preferences.IScopeContext#getName()
	 */
	public String getName() {
		return SCOPE;
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IScopeContext#getLocation()
	 */
	public IPath getLocation() {
		IPath result = null;
		URL url = InternalPlatform.getDefault().getUserLocation().getURL();
		if (url != null) {
			result = new Path(url.getFile());
			if (result.isEmpty())
				result = null;
		}
		return result;
	}
}
