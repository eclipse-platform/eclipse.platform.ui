/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.resources;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.BaserevInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.NotifyInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * Represents handles to CVS resource on the local file system. Synchronization
 * information is taken from the CVS subdirectories. 
 */
public class EclipseFile extends EclipseResource implements ICVSFile {

	private static final String TEMP_FILE_EXTENSION = ".tmp";//$NON-NLS-1$
	private static final IPath PROJECT_META_DATA_PATH = new Path(".project");//$NON-NLS-1$
	
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
			throw CVSException.wrapException(resource, Policy.bind("EclipseFile_Problem_deleting_resource", resource.getFullPath().toString(), e.getStatus().getMessage()), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public long getSize() {
		return getIOFile().length();	
	}

	public InputStream getContents() throws CVSException {
 		try {
			return getIFile().getContents();
		} catch (CoreException e) {
 			throw CVSException.wrapException(resource, Policy.bind("EclipseFile_Problem_accessing_resource", resource.getFullPath().toString(), e.getStatus().getMessage()), e); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
	
	/*
	 * @see ICVSFile#getTimeStamp()
	 */
	public Date getTimeStamp() {
		File ioFile = getIOFile();
		if (ioFile == null) {
			// If there is no file, return the same timestamp as ioFile.lastModified() would
			return new Date(0L);
		}			
		return new Date((ioFile.lastModified()/1000)*1000);
	}
 
	/*
	 * @see ICVSFile#setTimeStamp(String)
	 */
	public void setTimeStamp(Date date) throws CVSException {
		long time;
		if (date == null) {
			time = System.currentTimeMillis();
		} else {
			time = date.getTime();
		}
		getIOFile().setLastModified(time);
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
	 * @see ICVSFile#isModified()
	 */
	public boolean isModified() throws CVSException {
		ResourceSyncInfo info = getSyncInfo();
		if (!exists() || !isManaged(info)) {
			return true;
		} else {
			// consider a merged file as always modified.
			if(info.isMerged()) return true;
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
	 * @see ICVSResource#accept(ICVSResourceVisitor, boolean)
	 */
	public void accept(ICVSResourceVisitor visitor, boolean recurse) throws CVSException {
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
	public void setContents(InputStream stream, int responseType, boolean keepLocalHistory, IProgressMonitor monitor) throws CVSException {
		try {
			IFile file = getIFile();
			if (PROJECT_META_DATA_PATH.equals(file.getFullPath().removeFirstSegments(1))) {
				responseType = UPDATED;
			}
			switch (responseType) {
				case UPDATED:
					if (resource.exists()) {
						file.setContents(stream, true /*force*/, true /*keep history*/, monitor);
						break;
					}
				case CREATED: // creating a new file so it should not exist locally
					file.create(stream, false /*force*/, monitor);
					break;
				case MERGED: // merging contents into a file that exists locally
					// Ensure we don't leave the file in a partially written state
					IFile tempFile = file.getParent().getFile(new Path(file.getName() + TEMP_FILE_EXTENSION));
					monitor.beginTask(null, 100);
					if (tempFile.exists()) 
						tempFile.delete(true, Policy.subMonitorFor(monitor, 25));
					tempFile.create(stream, true /*force*/, Policy.subMonitorFor(monitor, 25));
					file.delete(false, true, Policy.subMonitorFor(monitor, 25));
					tempFile.move(new Path(file.getName()), true /*force*/, false /*history*/, Policy.subMonitorFor(monitor, 25));
					monitor.done();
					break;
				case UPDATE_EXISTING: // creating a new file so it should exist locally
					file.setContents(stream, true /*force*/, true /*keep history*/, monitor);
					break;
			}
		} catch(CoreException e) {
			throw CVSException.wrapException(resource, Policy.bind("EclipseFile_Problem_writing_resource", e.getMessage(), e.getStatus().getMessage()), e); //$NON-NLS-1$
		}
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
	public IFile getIFile() {
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
	/**
	 * @see ICVSFile#getLogEntries(IProgressMonitor)
	 */
	public ILogEntry[] getLogEntries(IProgressMonitor monitor)	throws TeamException {
		ResourceSyncInfo info = getSyncInfo();
		if(isManaged(info) && !info.isAdded()) {
			ICVSRemoteResource remoteFile = CVSWorkspaceRoot.getRemoteResourceFor(resource);
			return ((ICVSRemoteFile)remoteFile).getLogEntries(monitor);
		}
		return new ILogEntry[0];
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#setNotifyInfo(NotifyInfo)
	 */
	public void setNotifyInfo(NotifyInfo info) throws CVSException {
		if (isManaged()) {
			EclipseSynchronizer.getInstance().setNotifyInfo(resource, info);
			// On an edit, the base should be cached
			// On an unedit, the base should be restored (and cleared?)
			// On a commit, the base should be cleared
		}
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#getNotifyInfo()
	 */
	public NotifyInfo getNotifyInfo() throws CVSException {
		if (isManaged()) {
			return EclipseSynchronizer.getInstance().getNotifyInfo(resource);		
		}
		return null;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#setNotifyInfo(NotifyInfo)
	 */
	public void setBaserevInfo(BaserevInfo info) throws CVSException {
		if (isManaged()) {
			if (info == null) {
				EclipseSynchronizer.getInstance().deleteBaserevInfo(resource);
				EclipseSynchronizer.getInstance().deleteFileFromBaseDirectory(getIFile(), null);
			} else
				EclipseSynchronizer.getInstance().setBaserevInfo(resource, info);
		}
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#getNotifyInfo()
	 */
	public BaserevInfo getBaserevInfo() throws CVSException {
		if (isManaged()) {
			return EclipseSynchronizer.getInstance().getBaserevInfo(resource);
		}
		return null;
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#checkout(int)
	 */
	public void edit(int notifications, IProgressMonitor monitor) throws CVSException {
		if (!isReadOnly()) return;
		ResourceSyncInfo info = getSyncInfo();
		if (info == null || info.isAdded()) return;
		
		// convert the notifications to internal form
		char[] internalFormat;
		if (notifications == NO_NOTIFICATION) {
			internalFormat = null;
		} else if (notifications == NOTIFY_ON_ALL) {
			internalFormat = NotifyInfo.ALL;
		} else {
			List notificationCharacters = new ArrayList();
			if ((notifications & NOTIFY_ON_EDIT) >0) 
				notificationCharacters.add(new Character(NotifyInfo.EDIT));
			if ((notifications & NOTIFY_ON_UNEDIT) >0) 
				notificationCharacters.add(new Character(NotifyInfo.UNEDIT));
			if ((notifications & NOTIFY_ON_COMMIT) >0) 
				notificationCharacters.add(new Character(NotifyInfo.COMMIT));
			internalFormat = new char[notificationCharacters.size()];
			for (int i = 0; i < internalFormat.length; i++) {
				internalFormat[i] = ((Character)notificationCharacters.get(i)).charValue();
			}
		}
		
		// record the notification
		NotifyInfo notifyInfo = new NotifyInfo(getName(), NotifyInfo.EDIT, new Date(), internalFormat);
		setNotifyInfo(notifyInfo);
		
		EclipseSynchronizer.getInstance().copyFileToBaseDirectory(getIFile(), monitor);
		setBaserevInfo(new BaserevInfo(getName(), info.getRevision()));
		
		// allow editing
		setReadOnly(false);
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#uncheckout()
	 */
	public void unedit(IProgressMonitor monitor) throws CVSException {
		if (isReadOnly()) return;
		
		// record the notification
		NotifyInfo info = getNotifyInfo();
		if (info != null && info.getNotificationType() == NotifyInfo.EDIT) {
			info = null;
		} else {
			info = new NotifyInfo(getName(), NotifyInfo.UNEDIT, new Date(), null);
		}
		setNotifyInfo(info);
		
		EclipseSynchronizer.getInstance().restoreFileFromBaseDirectory(getIFile(), monitor);
		setBaserevInfo(null);
		
		// prevent editing
		setReadOnly(true);
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#notificationCompleted()
	 */
	public void notificationCompleted() throws CVSException {
		EclipseSynchronizer.getInstance().deleteNotifyInfo(resource);
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#getPendingNotification()
	 */
	public NotifyInfo getPendingNotification() throws CVSException {
		return getNotifyInfo();
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#committed(org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo)
	 */
	public void committed(ResourceSyncInfo info) throws CVSException {
		setSyncInfo(info);
		clearCachedBase();
	}
	
	private void clearCachedBase() throws CVSException {
		BaserevInfo base = getBaserevInfo();
		if (base != null) {
			setBaserevInfo(null);
			setReadOnly(true);
		}
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSResource#unmanage(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void unmanage(IProgressMonitor monitor) throws CVSException {
		super.unmanage(monitor);
		clearCachedBase();
	}

}