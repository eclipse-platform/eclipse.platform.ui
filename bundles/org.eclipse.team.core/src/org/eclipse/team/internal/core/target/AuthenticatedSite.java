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
package org.eclipse.team.internal.core.target;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * This is a layer that contains anything which a Target Site implementation that uses authentication would need to have.
 */
public abstract class AuthenticatedSite extends Site {
	
	protected String username;
	protected String password;

	/**
	 * @see org.eclipse.team.internal.core.target.Site#dispose()
	 */
	public void dispose() throws TeamException {
		try {
			Platform.flushAuthorizationInfo(getURL(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (CoreException e) {
			throw new TeamException(e.getStatus());
		}
	}

	/**
	 * Gets the username.
	 * @return Returns a String
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Gets the password.
	 * @return Returns a String
	 */
	public String getPassword() {
		return password;
	}

	public void setUsername(String name) throws TeamException {
		Map authInfo=Platform.getAuthorizationInfo(getURL(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (authInfo==null) authInfo=new HashMap(2);
		authInfo.put("name", name); //$NON-NLS-1$
		try {
			Platform.flushAuthorizationInfo(getURL(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
			Platform.addAuthorizationInfo(getURL(), "", "", authInfo); //$NON-NLS-1$ //$NON-NLS-2$
			this.username=name;
		} catch (CoreException e) {
			throw TeamPlugin.wrapException(e);
		} 
	}

	public void setPassword(String password) throws TeamException {
		Map authInfo=Platform.getAuthorizationInfo(getURL(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (authInfo==null) authInfo=new HashMap(2);
		authInfo.put("password", password); //$NON-NLS-1$
		try {
			Platform.flushAuthorizationInfo(getURL(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
			Platform.addAuthorizationInfo(getURL(), "", "", authInfo); //$NON-NLS-1$ //$NON-NLS-2$
			this.password=password;
		} catch (CoreException e) {
			throw TeamPlugin.wrapException(e);
		} 
	}
	
}
