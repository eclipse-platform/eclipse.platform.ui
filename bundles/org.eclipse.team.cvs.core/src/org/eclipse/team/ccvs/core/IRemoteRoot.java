package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
 
 /**
  * This interface represents a repository root. As such, it provides
  * access to the information required to connect to the repository and also
  * allows the retrieval of the top level modules either from HEAD or for a
  * particular version or branch tag.
  * 
  * Clients are not expected to implement this interface.
  */
public interface IRemoteRoot extends IRemoteFolder {
	
	/**
	 * port value which indicates to the default port
	 */
	public static int DEFAULT_PORT = 0;
	
	/**
	 * Returns the name of the method used to connect to the
	 * host (e.g. pserver, ext, etc.).
	 * 
	 * @return the connection method name.
	 */
	public String getConnectionMethod();
	
	/**
	 * Returns the name of the host where the remote root is located.
	 * 
	 * @return the host name of the server.
	 */
	public String getHost();
	
	/**
	 * Returns the members in the respository that are tagged with the given tag.
	 * The children of any resource returned by this method will also have the associated tag.
	 * Since CVS doesn't tag folders, all folders will be included while
	 * only files with the given tag are included.
	 */
	public IRemoteResource[] getMembers(final String tagName, final IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Returns the port used to connect to the host.
	 * 
	 * @return the port used to connect to the server or <code>USE_DEFAULT_PORT</code>
	 * if the default port for the connection method is to be used.
	 */
	public int getPort();
	
	/**
	 * Return the location of the repository on the server.
	 * 
	 * @return the server directory path for the repository location

	 */
	public String getRepositoryPath();
	
	/**
	 * Returns the username used to connect to the host.
	 * 
	 * @return the username for the host connection.
	 */
	public String getUser();
}

