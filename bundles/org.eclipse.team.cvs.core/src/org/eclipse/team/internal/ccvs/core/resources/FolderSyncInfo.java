package org.eclipse.team.internal.ccvs.core.resources;

import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSException;

public class FolderSyncInfo {

	private String repository;
	private String root;
	private CVSEntryLineTag tag;
	private boolean isStatic;

	public FolderSyncInfo() {
	}

	public FolderSyncInfo(String repo, String root, boolean isStatic) {
		this.repository = repo;
		this.root = root;
		this.isStatic = isStatic;
	}
	
	public FolderSyncInfo(String repo, String root, String entryLineTag, boolean isStatic) {
		this(repo, root, isStatic);
		if(entryLineTag!=null) {
			this.tag = new CVSEntryLineTag(entryLineTag);
		}
	}		
	
	public FolderSyncInfo(String repo, String root, CVSTag tag, boolean isStatic) {
		this(repo, root, isStatic);
		if (tag != null)
			this.tag = new CVSEntryLineTag(tag);
	}		

	/**
	 * Gets the root.
	 * @return Returns a String
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * Sets the root.
	 * @param root The root to set
	 */
	public void setRoot(String root) {
		this.root = root;
	}

	/**
	 * Gets the tag.
	 * @return Returns a String
	 */
	public CVSEntryLineTag getTag() {
		return tag;
	}

	/**
	 * Sets the tag for the folder.  The provided tag must not be null.
	 * @param tag The tag to set
	 */
	public void setTag(CVSTag tag) {
		this.tag = new CVSEntryLineTag(tag);
	}

	/**
	 * Gets the repository.
	 * @return Returns a String
	 */
	public String getRepository() {
		return repository;
	}

	/**
	 * Sets the repository.
	 * @param repository The repository to set
	 */
	public void setRepository(String repository) {
		this.repository = repository;
	}
	/**
	 * Gets the isStatic.
	 * @return Returns a boolean
	 */
	public boolean getIsStatic() {
		return isStatic;
	}

	/**
	 * Sets the isStatic.
	 * @param isStatic The isStatic to set
	 */
	public void setIsStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}
	
	/**
	 * Compute the remote-location out of root and repository
	 */
	public String getRemoteLocation() throws CVSException {
		
		String result;
		
		try {
			result = getRoot().substring(getRoot().indexOf("@")+1);
			result = result.substring(result.indexOf(":")+1);
			result = result + "/" + getRepository();
		} catch (IndexOutOfBoundsException e) {
			throw new CVSException("Maleformed root");
		}
		
		return result;
	}
}

