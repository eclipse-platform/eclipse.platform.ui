package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Map;

import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.api.FolderProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;

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
	
	private Map children;
	protected String remote;
	protected String tag;
	
	public RemoteManagedFolder(String name, ICVSRepositoryLocation repository, String remote) {
		this(name, repository, remote, null);
	}
	
	public RemoteManagedFolder(String name, ICVSRepositoryLocation repository, String remote, String[] children) {
		this(name, repository, remote, children, null);
	}

	public RemoteManagedFolder(String name, ICVSRepositoryLocation repository, String remote, String[] children, String tag) {
		super(name, null, repository);
		this.remote = remote;
		this.tag = tag;
		if (children != null) {
			this.children = new HashMap();
			for (int i=0;i<children.length;i++)
				this.children.put(children[i], new RemoteManagedFile(children[i], this, repository));
		}
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
		if (children == null)
			return new IManagedFile[0];
		else
			return (IManagedFile[])children.entrySet().toArray(new IManagedFile[children.size()]);
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
		if (children != null) {
			IManagedResource resource = (IManagedResource) children.get(path);
			if (resource != null)
				return resource;
		}
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
		FolderProperties fp = new FolderProperties(repository.getLocation(), remote, false);
		if (tag != null)
			fp.setTag(tag);
		return fp;
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

