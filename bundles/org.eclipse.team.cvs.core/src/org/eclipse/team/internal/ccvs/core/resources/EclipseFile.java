package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.EntryFileDateFormat;

/**
 * Represents handles to CVS resource on the local file system. Synchronization
 * information is taken from the CVS subdirectories. 
 */
class EclipseFile extends EclipseResource implements ICVSFile {

	private static final String TEMP_FILE_EXTENSION = ".tmp";
	
	/**
	 * Create a handle based on the given local resource.
	 */
	protected EclipseFile(IFile file) {
		super(file);
	}

	/*
	 * @see ICVSResource#delete()
	 */
	public void delete() throws CVSException {
		try {
			((IFile)resource).delete(false /*force*/, true /*keepHistory*/, null);
		} catch(CoreException e) {
			throw CVSException.wrapException(resource, Policy.bind("EclipseFile_Problem_deleting_resource", resource.getFullPath().toString()), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public long getSize() {
		return getIOFile().length();	
	}

	public InputStream getInputStream() throws CVSException {
 		try {
			return getIFile().getContents();
		} catch (CoreException e) {
 			throw CVSException.wrapException(resource, Policy.bind("EclipseFile_Problem_accessing_resource", resource.getFullPath().toString()), e); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
	
	public OutputStream getOutputStream(final int responseType, final boolean keepLocalHistory) throws CVSException {
		return new ByteArrayOutputStream() {
			public void close() throws IOException {
				try {
					IFile file = getIFile();
					if (responseType == CREATED || (responseType == UPDATED && ! resource.exists())) {
						file.create(new ByteArrayInputStream(toByteArray()), false /*force*/, null);
					} else if(responseType == UPDATE_EXISTING) {
						file.setContents(new ByteArrayInputStream(toByteArray()), false /*force*/, keepLocalHistory /*keep history*/, null);
					} else {
						
						file.setContents(new ByteArrayInputStream(toByteArray()), false /*force*/, keepLocalHistory /*keep history*/, null);
						
//						// Ensure we don't leave the file in a partially written state
//						IFile tempFile = file.getParent().getFile(new Path(file.getName() + TEMP_FILE_EXTENSION));
//						tempFile.create(new ByteArrayInputStream(toByteArray()), true /*force*/, null);
//						file.delete(false, true, null);
//						tempFile.move(new Path(file.getName()), true, true, null);
					}
				} catch(CoreException e) {
					throw new IOException(Policy.bind("EclipseFile_Problem_creating_resource", e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
				} finally {
					super.close();
				}
			}
		};
	}
	
	/*
	 * @see ICVSFile#getAppendingOutputStream()
	 */
	public OutputStream getAppendingOutputStream() throws CVSException {
		return new ByteArrayOutputStream() {
			public void close() throws IOException {
				try {
					IFile file = getIFile();
					if(resource.exists()) {
						file.appendContents(new ByteArrayInputStream(toByteArray()), false /*force*/, true /*keep history*/, null);
					} else {
						file.create(new ByteArrayInputStream(toByteArray()), false /*force*/, null);
					}
				} catch(CoreException e) {
					throw new IOException(Policy.bind("EclipseFile_Problem_appending_to_resource", e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
				} finally {
					super.close();
				}
			}
		};
	}
	
	/*
	 * @see ICVSFile#getTimeStamp()
	 */
	public String getTimeStamp() {						
		EntryFileDateFormat timestamp = new EntryFileDateFormat();		
		return timestamp.format(new Date(getIOFile().lastModified()));
	}
 
	/*
	 * @see ICVSFile#setTimeStamp(String)
	 */
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
		getIOFile().setLastModified(millSec);
		try {
			// Needed for workaround to Platform Core Bug #
			resource.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}		
	}

	/*
	 * @see ICVSResource#isFolder()
	 */
	public boolean isFolder() {
		return false;
	}
	
	/*
	 * @see ICVSFile#isDirty()
	 */
	public boolean isDirty() throws CVSException {
		if (!exists() || !isManaged()) {
			return true;
		} else {
			ResourceSyncInfo info = getSyncInfo();
			if (info.isAdded()) return false;
			if (info.isDeleted()) return true;
			return !getTimeStamp().equals(info.getTimeStamp());
		}
	}

	/*
	 * @see ICVSFile#isModified()
	 */
	public boolean isModified() throws CVSException {
		if (!exists() || !isManaged()) {
			return true;
		} else {
			ResourceSyncInfo info = getSyncInfo();
			return !getTimeStamp().equals(info.getTimeStamp());
		}
	}
	
	/*
	 * @see ICVSResource#accept(ICVSResourceVisitor)
	 */
	public void accept(ICVSResourceVisitor visitor) throws CVSException {
		visitor.visitFile(this);
	}

	/*
	 * This is to be used by the Copy handler. The filename of the form .#filename
	 */
	public void copyTo(String filename) throws CVSException {
		try {
			getIFile().copy(new Path(filename), true /*force*/, null);
		} catch(CoreException e) {
			throw new CVSException(e.getStatus());
		}
	}

	/*
	 * @see ICVSResource#getRemoteLocation()
	 */
	public String getRemoteLocation(ICVSFolder stopSearching) throws CVSException {
		return getParent().getRemoteLocation(stopSearching) + SEPARATOR + getName();
	}
				
	/*
	 * @see ICVSFile#setReadOnly()
	 */
	public void setReadOnly(boolean readOnly) throws CVSException {
		getIFile().setReadOnly(readOnly);
	}

	/*
	 * @see ICVSFile#isReadOnly()
	 */
	public boolean isReadOnly() throws CVSException {
		return getIFile().isReadOnly();
	}
	
	/*
	 * Typecasting helper
	 */
	private IFile getIFile() {
		return (IFile)resource;
	}	
	
	/*
	 * To allow accessing size and timestamp for the underlying java.io.File
	 */
	private File getIOFile() {
		IPath location = resource.getLocation();
		if(location!=null) {
			return location.toFile();
		}
		return null;
	}
}