package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.CVSFileException;
import org.eclipse.team.internal.ccvs.core.resources.api.CVSFileNotFoundException;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * Implements the IManagedResource interface on top of an 
 * instance of the ICVSResource interface
 * 
 * @see IManagedResource
 */
abstract class ManagedResource implements IManagedResource {

	static final String PLATFORM_NEWLINE = System.getProperty("line.separator");
	static final String SERVER_NEWLINE = "\n";
	
	static final byte[] PLATFORM_NEWBYTE = PLATFORM_NEWLINE.getBytes();
	static final byte[] SERVER_NEWBYTE = SERVER_NEWLINE.getBytes();

	/**
	 * Constructor for ManagedResource
	 */
	ManagedResource() {
	}

	/**
	 * Get the extention of the path of resource
	 * relative to the path of root
	 * 
	 * @throws CVSException if root is not a root-folder of resource
	 */
	public String getRelativePath(IManagedFolder root) 
		throws CVSException {
		
		ManagedResource rootFolder;
		
		try {
			rootFolder = (ManagedResource)root;
		} catch (ClassCastException e) {
			throw new CVSException(0,0,"two different implementations of IManagedResource used",e);
		}
		
		return getRelativePath(getCVSResource().getPath(), rootFolder.getCVSResource().getPath());
		
	}

	/**
	 * Get the extention of the path of resource
	 * relative to the path of root
	 * 
	 * seperator is the seperation between the different folders
	 * 
	 * @throws CVSException if root is not a root-folder of resource
	 */
	public static String getRelativePath(String resourceName, String rootName) 
		throws CVSException {

		String relativePath;

		if (!resourceName.startsWith(rootName)) {
			throw new CVSException("Internal error, resource does not start with root.");
		}
		
		// Otherwise we would get an ArrayOutOfBoundException
		// in case of two equal Resources
		if (rootName.length() == resourceName.length()) {
			return "";
		}
		
		// Get rid of the seperator, that would be in the 
		// beginning, if we did not go from +1
		relativePath = resourceName.substring(rootName.length() + 1);
		return convertSeparatorOutgoing(relativePath);
		
	}

	/**
	 * @see IManagedResource#delete()
	 */
	public void delete() {
		getCVSResource().delete();
	}

	/**
	 * @see IManagedResource#exists()
	 */
	public boolean exists() {
		return getCVSResource().exists();
	}

	/**
	 * @see IManagedResource#getParent()
	 */
	public IManagedFolder getParent() {
		return new ManagedFolder(getCVSResource().getParent());
	}

	/**
	 * @see IManagedResource#getParent()
	 */
	ManagedFolder getInternalParent() {
		return new ManagedFolder(getCVSResource().getParent());
	}


	/**
	 * @see IManagedResource#getName()
	 */
	public String getName() {
		return getCVSResource().getName();
	}

	/**
	 * @see IManagedResource#isIgnored()
	 */
	public boolean isIgnored() throws CVSException {
		return (!isManaged() && ((ManagedFolder)getParent()).isIgnored(getName()));
	}
	
	/**
	 * Create a IManagedFolder from a CVSFolder
	 */
	public static IManagedFolder createResourceFrom(ICVSFolder folder) {
		return new ManagedFolder(folder);
	}
	
	/**
	 * Create a IManagedFile form a CVSFile
	 * 
	 * For internal use only
	 */
	public static IManagedFile createResourceFrom(ICVSFile file) {
		return new ManagedFile(file);
	}
	
	/** 
	 * Clean up incoming path
	 * replaces "/" and "\\" for ICVSResource.seperator
	 */
	static String convertSeparatorIncoming(String path) {
		return convertSeperator(path, ICVSResource.seperator);	
	}
	
	/** 
	 * Clean up outgoing path
	 * replaces "/" and "\\" for this.seperator
	 */
	static String convertSeparatorOutgoing(String path) {
		return convertSeperator(path, separator);	
	}
	
	/**
	 * replaces "/" and "\\" for newSeperator
	 * @param newSeperator has to be "/" or "\\"
	 */
	private static String convertSeperator(String path,String newSeperator) {

		Assert.isTrue(newSeperator.equals("/") || newSeperator.equals("\\"));

		if (newSeperator.equals("/")) {
			return path.replace('\\','/');
		} else {
			return path.replace('/','\\');
		}
	}
		
	/**
	 * Throws an CVSFileNotFoundException if exists() = false
	 */
	void exceptionIfNotExists() throws CVSFileNotFoundException {
		if (!exists()) {
			throw new CVSFileNotFoundException(getName() + " does not exist");
		}
	}

	/**
	 * Two ManagedResources are equal, if there cvsResources are
	 * equal (and that is, if the point to the same file)
	 */
	public boolean equals(Object obj) {
		
		if (!(obj instanceof ManagedResource)) {
			return false;
		} else {
			return getCVSResource().equals(((ManagedResource) obj).getCVSResource());
		}
	}

	/**
	 * Comparing for the work with the sets,
	 * 
	 * The coparison is done by the underlying cvsResources.
	 */
	public int compareTo(Object obj) {
		if (!(obj instanceof ManagedResource)) {
			return -1;
		} else {
			return getCVSResource().compareTo(((ManagedResource) obj).getCVSResource());
		}
	}

	/**
	 * Generate a Standard CVSException for an
	 * IOException
	 * 
	 * Copied from CVSResource, we might have other texts here
	 */
	protected static CVSException wrapException(IOException e) {
		return new CVSException(IStatus.ERROR,
								CVSException.IO_FAILED,
								"An IOException occured while using your file-system.",
								e);
	}
	
	/**
	 * In order not to intrudce an new resource variable in this class we 
	 * have an abstract method that returns the resource.
	 * 
	 * As the resource could be of two different types, we do not want to
	 * save it here and do the cast in the Folder or the File
	 */
	public abstract ICVSResource getCVSResource();
	
	/**
	 * Implement the hashcode on the underlying strings, like it
	 * is done in the equals.
	 */
	public int hashCode() {
		return getCVSResource().hashCode();
	}
	/**
	 * Comparing for the work with the sets,
	 * 
	 * The coparison is done by the paths.
	 *
	public int compareTo(Object obj) {
		return cvsResource.compareTo(obj);
	}
	*/

}

