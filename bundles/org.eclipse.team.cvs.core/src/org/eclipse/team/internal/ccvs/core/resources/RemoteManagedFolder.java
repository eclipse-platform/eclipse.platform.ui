package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.api.FolderProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;

/**
 * This class can be used to pass an empty folder to CVS in order to see
 * what children the folder has. The command equivalent where this is applicable
 * is "cvs -n update" when in a CVS managed directory.
 * 
 * If given the name of a child file, this class can also be used to fetch the
 * contents of the file from the server. This is accomplished by associating
 * a single instance of RemoteManagedFile with the RemoteManagedFolder.
 */
public class RemoteManagedFolder extends RemoteManagedResource implements IManagedFolder {
	
	// NIK: Comment for the "one child" solution ?
	private RemoteManagedFile child;
	protected String remote;
	
	public RemoteManagedFolder(String name, ICVSRepositoryLocation repository, String remote) {
		this(name, repository, remote, null);
	}
	
	public RemoteManagedFolder(String name, ICVSRepositoryLocation repository, String remote, String child) {
		super(name, null, repository);
		this.remote = remote;
		if (child != null)
			this.child = new RemoteManagedFile(child, this, repository);
	}
	
	/**
	 * @see IManagedFolder#getFolders()
	 */
	public IManagedFolder[] getFolders() throws CVSException {
		return new IManagedFolder[0];
	}

	/**
	 * @see IManagedFolder#getFiles()
	 */
	public IManagedFile[] getFiles() throws CVSException {
		if (child == null)
			return new IManagedFile[0];
		else
			return new IManagedFile[] {child};
	}

	/**
	 * @see IManagedFolder#getFolder(String)
	 */
	public IManagedFolder getFolder(String name) throws CVSException {
		if (name.equals(Client.CURRENT_LOCAL_FOLDER) || name.equals(Client.CURRENT_LOCAL_FOLDER + Client.SERVER_SEPARATOR))
			return this;
		throw new CVSException(Policy.bind("RemoteManagedFolder.invalidChild", new Object[] {name}));
	}

	/**
	 * @see IManagedFolder#getFile(String)
	 */
	public IManagedFile getFile(String name) throws CVSException {
		return (IManagedFile)getChild(name);
	}

	/**
	 * @see IManagedResource#isFolder()
	 */
	public boolean isFolder() {
		return true;
	}
	
	/**
	 * @see IManagedFolder#childExists(String)
	 */
	public boolean childExists(String path) {
		try {
			return getChild(path) != null;
		} catch (CVSException e) {
			return false;
		}
	}

	/**
	 * @see IManagedFolder#getChild(String)
	 */
	public IManagedResource getChild(String path) throws CVSException {
		if (path.equals(Client.CURRENT_LOCAL_FOLDER))
			return this;
		if ((child != null) && (path.equals(child.getName())))
			return child;
		throw new CVSException(Policy.bind("RemoteManagedFolder.invalidChild", new Object[] {name}));
	}

	/**
	 * @see IManagedFolder#mkdir()
	 */
	public void mkdir() throws CVSException {
		throw new CVSException(Policy.bind("RemoteManagedResource.invalidOperation"));
	}

	/**
	 * @see IManagedFolder#flush(boolean)
	 */
	public void flush(boolean deep) {
	}

	/**
	 * @see IManagedFolder#getFolderInfo()
	 */
	public FolderProperties getFolderInfo() throws CVSException {
		return new FolderProperties(repository.getLocation(), remote, false);
	}

	/**
	 * @see IManagedFolder#setFolderInfo(FolderProperties)
	 */
	public void setFolderInfo(FolderProperties folderInfo) throws CVSException {
	}

	/**
	 * @see IManagedFolder#setProperty(String, String[])
	 */
	public void setProperty(String key, String[] content) throws CVSException {
	}

	/**
	 * @see IManagedFolder#unsetProperty(String)
	 */
	public void unsetProperty(String key) throws CVSException {
	}

	/**
	 * @see IManagedFolder#getProperty(String)
	 */
	public String[] getProperty(String key) throws CVSException {
		throw new CVSException(Policy.bind("RemoteManagedResource.invalidOperation"));
	}

	/**
	 * @see IManagedFolder#isCVSFolder()
	 */
	public boolean isCVSFolder() throws CVSException {
		return true;
	}

	/**
	 * @see IManagedFolder#acceptChildren(IManagedVisitor)
	 */
	public void acceptChildren(IManagedVisitor visitor) throws CVSException {
		throw new CVSException(Policy.bind("RemoteManagedResource.invalidOperation"));
	}

	/**
	 * @see IManagedResource#getRemoteLocation(IManagedFolder)
	 */
	public String getRemoteLocation(IManagedFolder stopSearching) throws CVSException {
		return repository.getRootDirectory() + Client.SERVER_SEPARATOR + remote;
	}

}

