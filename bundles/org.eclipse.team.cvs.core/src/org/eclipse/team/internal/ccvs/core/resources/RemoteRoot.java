package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.ccvs.core.IRemoteRoot;

/**
 * This class provides the implementation of IRemoteRoot
 */
public class RemoteRoot extends RemoteFolder implements IRemoteRoot {

	private CVSRepositoryLocation location;
	
	/**
	 * Constructor for RemoteRoot.
	 * @param parent
	 * @param name
	 */
	public RemoteRoot(CVSRepositoryLocation location) {
		super(null, location.getLocation(), null);
		this.location = location;
	}

	/**
	 * Return the CVSRepositoryLocation representing the remote repository
	 */
	public CVSRepositoryLocation getConnection() {
		return location;
	}
	
	/**
	 * @see IRemoteRoot#getConnectionMethod()
	 */
	public String getConnectionMethod() {
		return location.getMethod().getName();
	}

	/**
	 * Get the full path for the receiver, starting at the root
	 */
	public String getFullPath() {
		return "";
	}
	
	/**
	 * @see IRemoteRoot#getHost()
	 */
	public String getHost() {
		return location.getHost();
	}

	/**
	 * @see IRemoteRoot#getPort()
	 */
	public int getPort() {
		return location.getPort();
	}
	
	/**
	 * @see IRemoteRoot#getRepositoryPath()
	 */
	public String getRepositoryPath() {
		return location.getRootDirectory();
	}

	/**
	 * @see IRemoteResource#getType()
	 */
	public int getType() {
		return ROOT;
	}
	
	/**
	 * @see IRemoteRoot#getUser()
	 */
	public String getUser() {
		return location.getUsername();
	}

}

