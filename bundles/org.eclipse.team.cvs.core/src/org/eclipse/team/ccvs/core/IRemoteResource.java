package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * The interface represents a resource that exists in a CVS repository.
 * It purpose is to provide information about the remote resource from
 * the repository.
 * 
 * Clients are not expected to implement this interface.
 */
public interface IRemoteResource extends IAdaptable {
	
	public static int FILE = IResource.FILE;
	public static int FOLDER = IResource.FOLDER;
	public static int ROOT = IResource.PROJECT;
	
	/**
	 * Return the name of the remote resource. 
	 * <p>
	 * For regular files and folders, <code>getName()</code> returns the 
	 * unqualified name of the resource. For other remote
	 * resources, such as a repository, name will be more complicated.
	 * 
	 * @return the name of the remote resource.
	 */
	public String getName();
	
	/**
	 * Return the parent folder of the remote resource. 
	 * 
	 * @return the parent of the remote resource.
	 */
	public IRemoteFolder getParent();
	
	/**
	 * Return the type of the resource. 
	 * 
	 * @return the type of the remote resource
	 * (either <code>ROOT</code>, <code>FILE</code> or <code>FOLDER</code>)
	 */
	public int getType();

}

