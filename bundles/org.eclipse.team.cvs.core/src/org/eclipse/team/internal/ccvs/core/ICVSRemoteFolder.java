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
public interface ICVSRemoteFolder extends ICVSRemoteResource {
	
	/**
	 * Get the members of the remote folder. 
	 * 
	 * <p>
	 * In the case of a simple folder, the <code>getMembers()</code> would return <code>ICVSRemoteResource</code>
	 * instances for each of the files and folders contained in the remote folder.
	 * </p>
	 * 
	 * @return an array of <code>ICVSRemoteResource</code> instances which can be cast to
	 * the appropriate sub-interface (<code>ICVSRemoteFolder</code> or <code>ICVSRemoteFile</code>) 
	 * based on the type of the resource returned by <code>getType()</code>.
	 * 
	 * @throws TeamException if problems occur contacting the server.
	 */
	public ICVSRemoteResource[] getMembers(IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Returns a new instance that is the same as the receiver except for the tag.
	 */
	public ICVSRemoteFolder forTag(String tagName);


}

