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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.api.CVSFileNotFoundException;
import org.eclipse.team.internal.ccvs.core.resources.api.FileProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;

/**
 * This class mimics an IManagedFile in order to retrieve the contents of a file
 * from the server without storing it in the local file system. It is used along with
 * RemoteManagedFolder by RemoteFile.
 */
public class RemoteManagedFile extends RemoteManagedResource implements IManagedFile {
	
	private FileProperties info;
	private ByteArrayOutputStream bos;
	
	public RemoteManagedFile(String name,  IManagedFolder parent, ICVSRepositoryLocation repository) {
		super(name, parent, repository);
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
		return info;
	}

	/**
	 * @see IManagedFile#setFileInfo(FileProperties)
	 */
	public void setFileInfo(FileProperties fileInfo) throws CVSException {
		info = fileInfo;
	}

	/**
	 * @see IManagedFile#sendTo(OutputStream, IProgressMonitor, boolean)
	 */
	public void sendTo(
		OutputStream outputStream,
		IProgressMonitor monitor,
		boolean binary)
		throws CVSException {
			
		throw new CVSException(Policy.bind("RemoteManagedResource.invalidOperation"));
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
		throw new CVSException(Policy.bind("RemoteManagedResource.invalidOperation"));
	}

	/**
	 * @see IManagedFile#getContent()
	 */
	public String[] getContent() throws CVSException {
		throw new CVSException(Policy.bind("RemoteManagedResource.invalidOperation"));
	}

	/**
	 * @see IManagedResource#getRemoteLocation(IManagedFolder)
	 */
	public String getRemoteLocation(IManagedFolder stopSearching) throws CVSException {
		throw new CVSException(Policy.bind("RemoteManagedResource.invalidOperation"));
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
	public InputStream getCachedContents() {
		return new ByteArrayInputStream(bos.toByteArray());
	}

}

