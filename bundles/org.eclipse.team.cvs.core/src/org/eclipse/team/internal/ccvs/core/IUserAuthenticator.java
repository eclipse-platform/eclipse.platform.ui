package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSException;

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
	public boolean authenticateUser(ICVSRepositoryLocation location, IUserInfo userInfo, boolean retry, String message) throws CVSException;
	
	/**
	 * Store the password for the given repository location. The password of the provided IUserInfo
	 * is also set by this operation.
	 * 
	 * @param location The repository location asspociated with the given username and password.
	 * @param userinfo The userinfo object containing the username
	 * @param password The password to be stored
	 * 
	 * @exception CVSException if there are problems caching the authorization information.
	 */
	public void cachePassword(ICVSRepositoryLocation location, IUserInfo userInfo, String password) throws CVSException;

	/**
	 * Retrieve the username and password for the given repository location into the provided IUserInfo object.
	 * 
	 * @param location The repository location asspociated with the given username and password.
	 * @param userinfo The userinfo object effected
	 * @return <code>true</code> if the username and password were set
	 * 
	 * @exception CVSException if there are problems retrieving the authorization information.
	 */
	public boolean retrievePassword(ICVSRepositoryLocation location, IUserInfo userInfo) throws CVSException;

	/**
	 * Dispose of any information associated with the given location.
	 * 
	 * @param location The repository location being disposed.
	 * 
	 * @exception CVSException if there are problems removing the authorization information.
	 */
	public void dispose(ICVSRepositoryLocation location) throws CVSException;
}