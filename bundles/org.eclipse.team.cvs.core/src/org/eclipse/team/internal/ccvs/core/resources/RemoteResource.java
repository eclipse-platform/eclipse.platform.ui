package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.ccvs.core.IRemoteFolder;
import org.eclipse.team.ccvs.core.IRemoteResource;
import org.eclipse.team.ccvs.core.IRemoteRoot;

/**
 * The purpose of this class and its subclasses is to implement the corresponding
 * IRemoteResource interfaces for the purpose of communicating information about 
 * resources that reside in a CVS repository but have not necessarily been loaded
 * locally.
 */
public abstract class RemoteResource extends PlatformObject implements IRemoteResource {

	protected String name;
	protected RemoteFolder parent;
	protected IRemoteRoot root;
	protected String tag;
	
	protected RemoteResource(RemoteFolder parent, String name) {
		this(parent, name, null);
	}
	
	protected RemoteResource(RemoteFolder parent, String name, String tag) {
		this.parent = parent;
		this.name = name;
		this.tag = tag;
	}
	
	/**
	 * @see IRemoteResource#getName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @see IRemoteResource#getParent()
	 */
	public IRemoteFolder getParent() {
		return parent;
	}
	
	/**
	 * Get the path of the parent, starting at the root
	 */
	public String getParentPath() {
		return parent.getFullPath();
	}
	
	/**
	 * Return the IRemoteRoot that is the ancestor of the receiver
	 */
	public IRemoteRoot getRemoteRoot() {
		return root;
	}
	
	/**
	 * Get the full path for the receiver, starting at the root
	 */
	public String getFullPath() {
		String parentPath = parent.getFullPath();
		if (parentPath.length() == 0)
			return getName();
		else
			return parentPath + Client.SERVER_SEPARATOR + getName();
	}
	
	/**
	 * Return the CVSRepositoryLocation representing the remote repository
	 */
	public CVSRepositoryLocation getConnection() {
		return parent.getConnection();
	}

	protected List getLocalOptionsForTag() {
		List localOptions = new ArrayList();
		if ((tag != null) && (!tag.equals("HEAD"))) {
			localOptions.add(Client.TAG_OPTION);
			localOptions.add(tag);
		}
		return localOptions;
	}
}

