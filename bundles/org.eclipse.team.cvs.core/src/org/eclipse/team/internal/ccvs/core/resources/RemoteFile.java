package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.ccvs.core.ILogEntry;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.api.CVSFileNotFoundException;
import org.eclipse.team.internal.ccvs.core.resources.api.FileProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;
import org.eclipse.team.internal.ccvs.core.response.IResponseHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.LogHandler;

/**
 * This class provides the implementation of ICVSRemoteFile and IManagedFile for
 * use by the repository and sync view.
 */
public class RemoteFile extends RemoteResource implements ICVSRemoteFile, IManagedFile  {

	// cache for file properties provided by cvs commands
	private FileProperties info;
	
	// cache for file contents received from the server
	private ByteArrayOutputStream bos;
	
	protected RemoteFolder parent;
	
	/**
	 * Constructor for RemoteFile.
	 */
	public RemoteFile(RemoteFolder parent, String name, String tag) {
		super(name, tag);
		this.parent = parent;
	}

 	/**
	 * @see IManagedResource#accept(IManagedVisitor)
	 */
	public void accept(IManagedVisitor visitor) throws CVSException {
		visitor.visitFile(this);
	}

	/**
	 * @see ICVSRemoteFile#getContents()
	 */
	public InputStream getContents(final IProgressMonitor monitor) {
			
		// Perform a "cvs update..."
		try {
			List localOptions = getLocalOptionsForTag();
			Client.execute(
				Client.UPDATE,
				Client.EMPTY_ARGS_LIST, 
				(String[])localOptions.toArray(new String[localOptions.size()]),
				new String[]{getName()}, 
				parent,
				monitor,
				getPrintStream(),
				(CVSRepositoryLocation)getRepository(),
				null);
			return getCachedContents();
		} catch(CVSException e) {
			return null;
		}
	}

	/**
	 * @see ICVSRemoteFile#getLogEntries()
	 */
	public ILogEntry[] getLogEntries(IProgressMonitor monitor) throws CVSException {
		
		// Perform a "cvs log..." with a custom message handler
		final List entries = new ArrayList();
		Client.execute(
			Client.LOG,
			Client.EMPTY_ARGS_LIST, 
			Client.EMPTY_ARGS_LIST,
			new String[]{getName()}, 
			parent,
			monitor,
			getPrintStream(),
			(CVSRepositoryLocation)getRepository(),
			new IResponseHandler[] {new LogHandler(this, entries)});
		return (ILogEntry[])entries.toArray(new ILogEntry[entries.size()]);
	}
	
	/**
	 * @see ICVSRemoteFile#getRevision()
	 */
	public String getRevision() {
		return tag;
	}
	
	public RemoteFile toRevision(String revision) {
		return new RemoteFile(parent, getName(), revision);
	}
	
		/**
	 * @see IManagedFile#getSize()
	 */
	public long getSize() {
		return 0;
	}

	/**
	 * @see IManagedFile#getFileInfo()
	 */
	public FileProperties getFileInfo() throws CVSException {
		if (info == null) {
			FileProperties properties =  new FileProperties();
			properties.setName(getName());
			properties.setTimeStamp("dummy");
			properties.setKeywordMode("-kb");
			properties.setVersion("0");
			properties.setPermissions("u=rw,g=rw,o=rw");
			if ((parent.tag != null) && !(parent.tag.equals("HEAD")))
				properties.setTag("T" + parent.tag);
			return properties;
		}
		return info;
	}

	/*
	 * @see IManagedResource#getParent()
	 */
	public IManagedFolder getParent() {
		return parent;
 	}
 	
	/**
	 * @see IManagedResource#getRelativePath(IManagedFolder)
	 */
	public String getRelativePath(IManagedFolder ancestor) throws CVSException {
		String result = parent.getRelativePath(ancestor);
		if (result.length() == 0)
			return getName();
		else
			return result + Client.SERVER_SEPARATOR + getName();
	}
	
	/**
	 * @see IManagedResource#getRemoteLocation(IManagedFolder)
	 */
	public String getRemoteLocation(IManagedFolder stopSearching) throws CVSException {
		return parent.getRemoteLocation(stopSearching) + Client.SERVER_SEPARATOR + getName();
	}
	
	/**
	 * Get the remote path for the receiver relative to the repository location path
	 */
	public String getRemotePath() {
		String parentPath = parent.getRemotePath();
		return parentPath + Client.SERVER_SEPARATOR + getName();
	}
	
	/**
	 * Return the server root directory for the repository
	 */
	public ICVSRepositoryLocation getRepository() {
		return parent.getRepository();
	}
	
	/**
	 * @see IManagedFile#setFileInfo(FileProperties)
	 */
	public void setFileInfo(FileProperties fileInfo) throws CVSException {
		info = fileInfo;
	}

	public void setRevision(String revision) {
		tag = revision;
	}
		
	/**
	 * @see IManagedFile#sendTo(OutputStream, IProgressMonitor, boolean)
	 */
	public void sendTo(
		OutputStream outputStream,
		IProgressMonitor monitor,
		boolean binary)
		throws CVSException {
			
		throw new CVSException(Policy.bind("RemoteResource.invalidOperation"));
	}

	/**
	 * @see IManagedFile#receiveFrom(InputStream, IProgressMonitor, long, boolean)
	 */
	public void receiveFrom(
		InputStream inputStream,
		IProgressMonitor monitor,
		long size,
		boolean binary,
		boolean readOnly)
		throws CVSException {
			
		// NOTE: This should be changed such that the client or connection handles
		// the proper transfer
		try {
			bos = new ByteArrayOutputStream();
			if (binary)
				ManagedFile.transferWithProgress(inputStream, bos, (long)size, monitor, "");
			else
				ManagedFile.transferText(inputStream, bos, (long)size, monitor, "", false);
		} catch (IOException ex) {
			throw ManagedFile.wrapException(ex);
		}
	}

	/**
	 * @see IManagedFile#getTimeStamp()
	 */
	public String getTimeStamp() throws CVSFileNotFoundException {
		return null;
	}

	/**
	 * @see IManagedFile#setTimeStamp(String)
	 */
	public void setTimeStamp(String date) throws CVSException {
	}

	/**
	 * @see IManagedFile#isDirty()
	 */
	public boolean isDirty() throws CVSException {
		return false;
	}

	/**
	 * @see IManagedFile#moveTo(IManagedFile)
	 */
	public void moveTo(IManagedFile mFile) throws CVSException, ClassCastException {		
		throw new CVSException(Policy.bind("RemoteResource.invalidOperation"));
	}

	/**
	 * @see IManagedFile#getContent()
	 */
	public String[] getContent() throws CVSException {
		throw new CVSException(Policy.bind("RemoteResource.invalidOperation"));
	}

	/**
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object arg0) {
		return 0;
	}
	
	/**
	 * Return an InputStream which contains the contents of the remote file.
	 */
	private InputStream getCachedContents() {
		InputStream is = new ByteArrayInputStream(bos.toByteArray());
		bos = null;
		return is;
	}
	/*
	 * @see IRemoteResource#members(IProgressMonitor)
	 */
	public IRemoteResource[] members(IProgressMonitor progress) throws TeamException {
		return new IRemoteResource[0];
	}

	/*
	 * @see IRemoteResource#isContainer()
	 */
	public boolean isContainer() {
		return false;
	}

	/*
	 * @see IManagedResource#isFolder()
	 */
	public boolean isFolder() {
		return false;
	}
}

