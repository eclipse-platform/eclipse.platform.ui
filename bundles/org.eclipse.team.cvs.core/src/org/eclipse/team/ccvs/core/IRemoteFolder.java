package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

 /**
  * This interface represents a remote folder in a repository. It provides
  * access to the members (remote files and folders) of a remote folder
  * 
  * Clients are not expected to implement this interface.
  */
public interface IRemoteFolder extends IRemoteResource {
	
	/**
	 * Get the members of the remote folder. 
	 * 
	 * <p>
	 * In the case of a simple folder, the <code>getMembers()</code> would return <code>IRemoteResource</code>
	 * instances for each of the files and folders contained in the remote folder.
	 * </p>
	 * 
	 * @return an array of <code>IRemoteResource</code> instances which can be cast to
	 * the appropriate sub-interface (<code>IRemoteFolder</code> or <code>IRemoteFile</code>) 
	 * based on the type of the resource returned by <code>getType()</code>.
	 * 
	 * @throws TeamException if problems occur contacting the server.
	 */
	public IRemoteResource[] getMembers(IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Returns a new instance that is the same as the receiver except for the tag.
	 */
	public IRemoteFolder forTag(String tagName);


}

