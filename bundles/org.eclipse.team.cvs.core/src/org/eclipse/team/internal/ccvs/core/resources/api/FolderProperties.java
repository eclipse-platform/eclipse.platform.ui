package org.eclipse.team.internal.ccvs.core.resources.api;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * FolderProperties bundels the informations about a folder that
 * are needed for the cvsClient.
 * It cares about loading saving this information 
 * in the folder the container belongs to.
 */
public class FolderProperties extends CVSProperties {
	
	public static final String REPOSITORY = "Repository";
	public static final String ROOT = "Root";
	public static final String STATIC = "Entries.Static";	
	public static final String seperator = "/";

	public FolderProperties() {
		super(new String[]{REPOSITORY,ROOT,STATIC});
	}	
	
	/**
	 * Create a new FolderProperties and load the information of the cvsFolder 
	 * into it.
	 * Does not save the cvsFolder in any way.
	 */
	public FolderProperties(String root, String repository, boolean staticFolder) {	
		this();
		setRoot(root);
		setRepository(repository);
		setStaticFolder(staticFolder);
	}
	
	/**
	 * Gets the repolsitory e.g. "proj1/folder1"
	 * @return Returns a String
	 */
	public String getRepository() {
		return getProperty(REPOSITORY);
	}
	/**
	 * Sets the repolsitory
	 * @param repolsitory e.g. "proj1/folder1"
	 * @throws CVSException on wrong parameter
	 */
	public void setRepository(String repository) {
		
		putProperty(REPOSITORY,repository);
	}

	/**
	 * Gets the root e.g. ":pserver:nkrambro@fiji:/home/nkrambro/repo"
	 * @return Returns a String
	 */
	public String getRoot() {
		return getProperty(ROOT);
	}
	/**
	 * Sets the root
	 * @param the Root of the Folder e.g. ":pserver:nkrambro@fiji:/home/nkrambro/repo"
	 * @throws CVSException on wrong parameter
	 */
	public void setRoot(String root) {
		putProperty(ROOT,root);
	}
	
	/**
	 * Returns the Location of the folder on the server constructed 
	 * using the root and repository.
	 * 
	 * For example, if the <code>root</code> is ":pserver:username@host:/cvs/root"
	 * and the <code>repository</code> is "proj1/folder1" then <code>getRemoteLocation()</code>
	 * returns "/cvs/root/proj1/folder1".
	 */
	public String getRemoteLocation() throws CVSException {

		String rootFolder;
		int start = getRoot().lastIndexOf(":");
		if (start == -1)
			throw new CVSException(Policy.bind("FolderProperties.invalidRoot", new Object[] {getRoot()}));
		rootFolder = getRoot().substring(start + 1);
		
		return rootFolder + seperator + getRepository();
	}
	
	/**
	 * Gets wheter the folder is static
	 * @return Returns a boolean
	 */
	public boolean getStaticFolder() {
		return getProperty(STATIC) != null;
	}
	
	/**
	 * Sets wheter the folder is static
	 * @param staticFolder The staticFolder to set
	 */
	public void setStaticFolder(boolean staticFolder) {
		if (staticFolder) {
			putProperty(STATIC,"");
		} else {
			putProperty(STATIC,null);
		}
	}

}

