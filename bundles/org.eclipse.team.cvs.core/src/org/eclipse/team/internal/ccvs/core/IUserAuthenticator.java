package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */



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