package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * This interface provides access to the specific portions of
 * the repository location string for use by connection methods
 * and the user authenticator.
 * 
 * It is not intended to implemented by clients.
 * 
 * @see IUserAuthenticator
 * @see IConnectionMethod
 */
public interface ICVSRepositoryLocation  extends IAdaptable {

	/**
	 * port value which indicates to a connection method to use the default port
	 */
	public static int USE_DEFAULT_PORT = 0;
	
	/**
	 * Return the connection method for making the connection
	 */
	public IConnectionMethod getMethod();
	
	/**
	 * Returns the host where the repository is located
	 */
	public String getHost();
	
	/**
	 * Returns the port to connect to or USE_DEFAULT_PORT if
	 * the connection method is to use its default port.
	 */
	public int getPort();
	
	/**
	 * Returns the root directory of the repository.
	 */
	public String getRootDirectory();
	
	/**
	 * Returns the string representing the receiver. This string
	 * should contain enough information to recreate the receiver.
	 */
	public String getLocation();

	/**
	 * Returns the immediate children of this location. If tag is <code>null</code> the
	 * HEAD branch is assumed.
	 * 
	 * @param tag the context in which to return the members (e.g. branch or version).
	 */
	public ICVSRemoteResource[] members(CVSTag tag, IProgressMonitor progress)  throws CVSException;
	
	/**
	 * Return the conection timeout value in milliseconds.
	 * A value of 0 means there is no timeout value.
	 */
	public int getTimeout();
	
	/**
	 * Return the information about the user as an IUserInfo.
	 * 
	 * This allows the querying of the user name and the setting
	 * of the username and password.
	 */
	public IUserInfo getUserInfo();
	
	/**
	 * Return the username 
	 */
	public String getUsername();
}

