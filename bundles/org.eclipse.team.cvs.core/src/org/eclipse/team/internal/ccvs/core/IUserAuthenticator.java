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
package org.eclipse.team.internal.ccvs.core;




/**
 * IUserAuthenticators are used to ensure that the user
 * is validated for access to a given repository.  The
 * user is prompted for a username and password as
 * appropriate for the given repository type.
 */
public interface IUserAuthenticator {
	/**
	 * Authenticates the user for access to a given repository.
	 * The obtained values for user name and password will be placed
	 * into the supplied user info object. Implementors are allowed to
	 * save user names and passwords. The user should be prompted for
	 * user name and password if there is no saved one, or if <code>retry</code>
	 * is <code>true</code>.
	 *
	 * @param location The repository location to authenticate the user for.
	 * @param info The object to place user validation information into.
	 * @param retry <code>true</code> if a previous attempt to log in failed.
	 * @param message An optional message to display if, e.g., previous authentication failed.
	 * @return true if the validation was successful, and false otherwise.
	 */
	public void promptForUserInfo(ICVSRepositoryLocation location, IUserInfo userInfo, String message) throws CVSException;
}
