package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;

/**
 * This class is the root of a hierarchy of IManagedResource which allows the retrieval
 * of information about remote resources without requiring the resources to exist locally.
 * As such, only the required methods of IManagedResource have implementations.
 */
public abstract class RemoteManagedResource implements IManagedResource {

	protected String name;
	protected IManagedFolder parent;
	protected ICVSRepositoryLocation repository;
	
	protected RemoteManagedResource(String name, IManagedFolder parent, ICVSRepositoryLocation repository) {
		this.name = name;
		this.repository = repository;
		this.parent = parent;
	}
	
	/**
	 * @see IManagedResource#getRelativPath(IManagedFolder)
	 */
	public String getRelativePath(IManagedFolder ancestor) throws CVSException {
		if (ancestor == this)
			return getName();
		throw new CVSException(Policy.bind("RemoteManagedResource.invalidOperation"));
	}

	/**
	 * @see IManagedResource#isFolder()
	 */
	public boolean isFolder() {
		return false;
	}

	/**
	 * @see IManagedResource#delete()
	 */
	public void delete() {
	}

	/**
	 * @see IManagedResource#exists()
	 */
	public boolean exists() {
		return true;
	}

	/**
	 * @see IManagedResource#getParent()
	 */
	public IManagedFolder getParent() {
		return null;
	}

	/**
	 * @see IManagedResource#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see IManagedResource#isIgnored()
	 */
	public boolean isIgnored() throws CVSException {
		return false;
	}
	
	/**
	 * @see IManagedResource#isManaged()
	 */
	public boolean isManaged() throws CVSException {
		return true;
	}

	/**
	 * @see IManagedResource#accept(IManagedVisitor)
	 */
	public void accept(IManagedVisitor visitor) throws CVSException {
		// We need to do nothing here
	}

	/**
	 * @see IManagedResource#unmanage()
	 */
	public void unmanage() throws CVSException {
		throw new CVSException(Policy.bind("RemoteManagedResource.invalidOperation"));
	}
	
	/**
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object arg0) {
		return 0;
	}

}

