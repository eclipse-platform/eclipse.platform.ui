package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.*;
import org.eclipse.team.internal.ccvs.core.util.EntryFileDateFormat;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * Represents handles to CVS resource on the local file system. Synchronization
 * information is taken from the CVS subdirectories. 
 * 
 * @see LocalFolder
 * @see LocalFile
 */
public class LocalFile extends LocalResource implements ICVSFile {
	/**
	 * Create a handle based on the given local resource.
	 */
	public LocalFile(File file) {
		super(file);
	}

	public long getSize() {
		return ioResource.length();
	}

	public InputStream getInputStream() throws CVSException {
 		try {
			return new FileInputStream(ioResource);
		} catch (IOException e) {
 			throw CVSException.wrapException(e);
 		}
 	}
	
	public OutputStream getOutputStream() throws CVSException {
		try {
			return new FileOutputStream(ioResource);
 		} catch (IOException e) {
 			throw CVSException.wrapException(e);
		}
	}
	
	public void setReadOnly() throws CVSException {
		ioResource.setReadOnly();
	}	

	public String getTimeStamp() throws CVSFileNotFoundException {						
		EntryFileDateFormat timestamp = new EntryFileDateFormat();		
		return timestamp.format(new Date(ioResource.lastModified()));
	}
 
	public void setTimeStamp(String date) throws CVSException {
		long millSec;		
		if (date==null) {
			// get the current time
			millSec = new Date().getTime();
		} else {
			try {
				EntryFileDateFormat timestamp = new EntryFileDateFormat();
				millSec = timestamp.toDate(date).getTime();
			} catch (ParseException e) {
				throw new CVSException(Policy.bind("LocalFile.invalidDateFormat", date), e); //$NON-NLS-1$
			}
		}		
		ioResource.setLastModified(millSec);
	}

	public boolean isFolder() {
		return false;
	}
	
	public boolean isDirty() throws CVSException {
		// XXX Is the below correct for isDirty?
		if (!exists() || !isManaged()) {
			return true;
		} else {
			ResourceSyncInfo info = getSyncInfo();
			if (info.isAdded()) return false;
			if (info.isDeleted()) return true;
			return !getTimeStamp().equals(info.getTimeStamp());
		}
	}

	public boolean isModified() throws CVSException {
		if (!exists() || !isManaged()) {
			return true;
		} else {
			ResourceSyncInfo info = getSyncInfo();
			return !getTimeStamp().equals(info.getTimeStamp());
		}
	}
	
	public void accept(ICVSResourceVisitor visitor) throws CVSException {
		visitor.visitFile(this);
	}

	/*
	 * This is to be used by the Copy handler. The filename of the form .#filename
	 */
	public void moveTo(String filename) throws CVSException {
		
		// Move the file to newFile (we know we do not need the
		// original any more anyway)
		// If this file exists then overwrite it
		LocalFile file;
		try {
			file = (LocalFile)getParent().getFile(filename);
		} catch(ClassCastException e) {
			throw CVSException.wrapException(e);
		}
		
		// We are deleting the old .#filename if it exists
		if (file.exists()) {
			file.delete();
		}
		
		boolean success = ioResource.renameTo(file.getFile());
		
		if (!success) {
			throw new CVSException(Policy.bind("LocalFile.moveFailed", ioResource.toString(), file.toString())); //$NON-NLS-1$
		}
	}

	File getFile() {
		return ioResource;
	}

	/**
	 * @see ICVSResource#getRemoteLocation()
	 */
	public String getRemoteLocation(ICVSFolder stopSearching) throws CVSException {
		return getParent().getRemoteLocation(stopSearching) + SEPARATOR + getName();
	}	
	/*
	 * @see ICVSResource#unmanage()
	 */
	public void unmanage() throws CVSException {
		CVSProviderPlugin.getSynchronizer().deleteResourceSync(ioResource);
	}
}