package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;

/**
 * The purpose of this class and its subclasses is to implement the corresponding
 * ICVSRemoteResource interfaces for the purpose of communicating information about 
 * resources that reside in a CVS repository but have not necessarily been loaded
 * locally.
 */
public abstract class RemoteResource extends PlatformObject implements ICVSRemoteResource, IManagedResource {

	protected String name;
	protected String tag;

	protected RemoteResource(String name, String tag) {
		this.name = name;
		this.tag = tag;
	}
	
	/**
	 * @see ICVSRemoteResource#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * Get the local options for including a tag in a CVS command
	 */
	protected List getLocalOptionsForTag() {
		List localOptions = new ArrayList();
		if ((tag != null) && (!tag.equals("HEAD"))) {
			localOptions.add(Client.TAG_OPTION);
			localOptions.add(tag);
		}
		return localOptions;
	}
		
	public abstract String getRemotePath();
	
	public abstract ICVSRepositoryLocation getRepository();
	
	/*
	 * @see IManagedResource#delete()
	 */
	public void delete() {
		// XXX we should know how to delete a remote?
	}

	/*
	 * @see IManagedResource#exists()
	 */
	public boolean exists() {
		// XXX perform silent checkout to test if this remote handle actually has a corresponding remote
		// resource
		return true;
	}

	/*
	 * @see IManagedResource#getParent()
	 */
	public IManagedFolder getParent() {
		throw new UnsupportedOperationException(Policy.bind("RemoteManagedResource.invalidOperation"));
	}

	/*
	 * @see IManagedResource#isIgnored()
	 */
	public boolean isIgnored() throws CVSException {
		return false;
	}

	/*
	 * @see IManagedResource#isManaged()
	 */
	public boolean isManaged() throws CVSException {
		return true;
	}

	/*
	 * @see IManagedResource#unmanage()
	 */
	public void unmanage() throws CVSException {
		throw new CVSException(Policy.bind("RemoteManagedResource.invalidOperation"));
	}

	/*
	 * @see IManagedResource#accept(IManagedVisitor)
	 */
	public void accept(IManagedVisitor visitor) throws CVSException {
		// We need to do nothing here
	}

	/*
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object arg0) {
		return 0;
	}
	
	
	protected PrintStream getPrintStream() {
		return CVSProviderPlugin.getProvider().getPrintStream();
	}

	/*
	 * @see IManagedResource#showDirty()
	 */
	public boolean showDirty() throws CVSException {
		return false;
	}

	/*
	 * @see IManagedResource#clearDirty(boolean)
	 */
	public void clearDirty(boolean up) throws CVSException {
	}

	/*
	 * @see IManagedResource#showManaged()
	 */
	public boolean showManaged() throws CVSException {
		return true;
	}

	/*
	 * @see IManagedResource#clearManaged()
	 */
	public void clearManaged() throws CVSException {
	}

	/*
	 * @see IManagedResource#getRelativePath(IManagedFolder)
	 */
	public String getRelativePath(IManagedFolder ancestor) throws CVSException {
		return null;
	}
}