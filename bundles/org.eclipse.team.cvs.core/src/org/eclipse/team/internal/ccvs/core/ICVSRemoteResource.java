package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.team.core.sync.IRemoteResource;

/**
 * The interface represents a resource that exists in a CVS repository.
 * It purpose is to provide information about the remote resource from
 * the repository.
 * 
 * Clients are not expected to implement this interface.
 */
public interface ICVSRemoteResource extends IRemoteResource {
	
	/**
	 * Return the repository
	 */
	public ICVSRepositoryLocation getRepository();
	
	/**
	 * Returns the parent of this remote resource or <code>null</code> if the
	 * remote resource does not have a parent.
	 */
	public ICVSRemoteResource getRemoteParent();
	
	/**
	 * Does the remote resource represented by this handle exist on the server. This
	 * method may contact the server and be long running.
	 */
	public boolean exists();
	
	/**
	 * Answers the repository relative path of this remote folder.
	 */
	public String getRelativePath();
	
	/**
	 * Compares two objects for equality; for cvs remote resources, equality is defined in 
	 * terms of their handles: same cvs resource type, equal relative paths, and
	 * for files, identical revision numbers. Remote resources are not equal to objects other 
	 * than cvs remote resources.
	 *
	 * @param other the other object
	 * @return an indication of whether the objects are equals
	 */
	public boolean equals(Object other);
}