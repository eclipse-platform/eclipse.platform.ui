package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * The purpose of this class and its subclasses is to implement the corresponding
 * ICVSRemoteResource interfaces for the purpose of communicating information about 
 * resources that reside in a CVS repository but have not necessarily been loaded
 * locally.
 */
public abstract class RemoteResource extends PlatformObject implements ICVSRemoteResource, ICVSResource {

	protected ResourceSyncInfo info;
	protected RemoteFolder parent;

	protected RemoteResource(RemoteFolder parent, String name, CVSTag tag, boolean isFolder) {
		info = new ResourceSyncInfo(name, isFolder);
		info.setTag(tag);
		this.parent = parent;
	}
	
	/*
	 * @see ICVSRemoteResource#getName()
	 */
	public String getName() {
		return info.getName();
	}

	/*
	 * @see ICVSRemoteResource#getParent()
	 */
	public ICVSRemoteResource getRemoteParent() {
		return parent;
	}

	/*
	 * Get the local options for including a tag in a CVS command
	 */
	protected List getLocalOptionsForTag() {
		List localOptions = new ArrayList();
		CVSTag tag = info.getTag();
		if ((tag != null) && (tag.getType() != tag.HEAD)) { 
			localOptions.add(Client.TAG_OPTION);
			localOptions.add(tag.getName()); 
		}
		return localOptions;
	}
		
	public abstract String getRemotePath();
	
	public abstract ICVSRepositoryLocation getRepository();
	
	/*
	 * @see ICVSResource#delete()
	 */
	public void delete() {
		// XXX we should know how to delete a remote?
	}

	/*
	 * @see ICVSResource#exists()
	 * 
	 * This method is used by the Command framework so it must return true so that 
	 * the proper information gets sent to the server.
	 */
	public boolean exists() {
		return true;
	}
	
	/*
	 * Non-API method that checks for existance remotely
	 */
	public boolean exists(IProgressMonitor monitor) throws CVSException {
		return parent.exists(this, monitor);
	}

	/*
	 * @see ICVSResource#getParent()
	 */
	public ICVSFolder getParent() {
		throw new UnsupportedOperationException(Policy.bind("RemoteManagedResource.invalidOperation"));
	}

	/*
	 * @see ICVSResource#isIgnored()
	 */
	public boolean isIgnored() throws CVSException {
		return false;
	}

	/*
	 * @see ICVSResource#isManaged()
	 */
	public boolean isManaged() {
		return true;
	}

	/*
	 * @see ICVSResource#unmanage()
	 */
	public void unmanage() throws CVSException {
		throw new CVSException(Policy.bind("RemoteManagedResource.invalidOperation"));
	}

	protected PrintStream getPrintStream() {
		return CVSProviderPlugin.getProvider().getPrintStream();
	}
	
	/*
	 * @see ICVSResource#getSyncInfo()
	 */
	public ResourceSyncInfo getSyncInfo() {
		return info;
	}
	/*
	 * @see ICVSResource#setSyncInfo(ResourceSyncInfo)
	 */
	public void setSyncInfo(ResourceSyncInfo info) {
		//this.info = info;
	}
}