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
import org.eclipse.core.resources.IResourceStatus;
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
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.BaserevInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
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
		
		// DECORATOR small optimization, we could check for existance only if getDirtyIndicator 
		// throws an exception.
		
		if (!exists()) return true;
		String indicator = EclipseSynchronizer.getInstance().getDirtyIndicator(getIFile());

		if (indicator != EclipseSynchronizer.RECOMPUTE_INDICATOR) {
			return indicator == EclipseSynchronizer.IS_DIRTY_INDICATOR;
		}
		
		// nothing cached, need to manually check (and record)
		ResourceSyncInfo info = getSyncInfo();
		if (info == null && isIgnored()) return false;
		// unmanaged files are reported as modified
		boolean dirty = computeModified(info);
		setModified(dirty);
		return dirty;
	}
	
	/*
	 * Deteremine if the receiver is modified when compared with the given sync
	 * info.
	 */
	private boolean computeModified(ResourceSyncInfo info) throws CVSException {
		if (info == null) return true;
		
		// isMerged() must be called because when a file is updated and merged by the cvs server the timestamps
		// are equal. Merged files should however be reported as dirty because the user should take action and commit
		// or review the merged contents.
		if(info.isMerged() || !exists()) return true;
		return !getTimeStamp().equals(info.getTimeStamp());
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
			String message = null;
			if (e.getStatus().getCode() == IResourceStatus.FAILED_READ_LOCAL) {
				// This error indicates that Core couldn't read from the server stream
				// The real reason will be in the message of the wrapped exception
				Throwable t = e.getStatus().getException();
				if (t != null) message = t.getMessage();
			}
			if (message == null) message = e.getMessage();
			throw CVSException.wrapException(resource, Policy.bind("EclipseFile_Problem_writing_resource", resource.getFullPath().toString(), message), e); //$NON-NLS-1$
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
		byte[] syncBytes = getSyncBytes();
		if(syncBytes != null && !ResourceSyncInfo.isAddition(syncBytes)) {
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
	public void edit(final int notifications, IProgressMonitor monitor) throws CVSException {
		if (!isReadOnly()) return;
		run(new ICVSRunnable() {
			public void run(IProgressMonitor monitor) throws CVSException {
				byte[] syncBytes = getSyncBytes();
				if (syncBytes == null || ResourceSyncInfo.isAddition(syncBytes)) return;
				
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
				
				// Only record the base if the file is not modified
				if (!isModified()) {
					EclipseSynchronizer.getInstance().copyFileToBaseDirectory(getIFile(), monitor);
					setBaserevInfo(new BaserevInfo(getName(), ResourceSyncInfo.getRevision(syncBytes)));
				}
				
				// allow editing
				setReadOnly(false);
			}
		}, monitor);
		
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#uncheckout()
	 */
	public void unedit(IProgressMonitor monitor) throws CVSException {
		if (isReadOnly()) return;
		run(new ICVSRunnable() {
			public void run(IProgressMonitor monitor) throws CVSException {
				// record the notification
				NotifyInfo info = getNotifyInfo();
				if (info != null && info.getNotificationType() == NotifyInfo.EDIT) {
					info = null;
				} else {
					info = new NotifyInfo(getName(), NotifyInfo.UNEDIT, new Date(), null);
				}
				setNotifyInfo(info);
					
				if (isModified()) {
					ResourceSyncInfo syncInfo = getSyncInfo();
					BaserevInfo baserevInfo = getBaserevInfo();
					EclipseSynchronizer.getInstance().restoreFileFromBaseDirectory(getIFile(), monitor);
					// reset any changes that may have been merged from the server
					if (!syncInfo.getRevision().equals(baserevInfo.getRevision())) {
						MutableResourceSyncInfo newInfo = syncInfo.cloneMutable();
						newInfo.setRevision(baserevInfo.getRevision());
						newInfo.setTimeStamp(getTimeStamp());
						newInfo.setDeleted(false);
						newInfo.reported(); // We report the change below
						setSyncInfo(newInfo);
					}
					// an unedited file is no longer modified
					setModified(false);
				}
				setBaserevInfo(null);
					
				// prevent editing
				setReadOnly(true);
			}
		}, monitor);
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
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#committed(String)
	 */
	public void checkedIn(String entryLine) throws CVSException {
		ResourceSyncInfo oldInfo = getSyncInfo();
		ResourceSyncInfo newInfo = null;
		boolean modified = false;
		if (entryLine == null) {
			// The file contents matched the server contents so no entry line was sent
			if (oldInfo == null) return;
			Date timeStamp = oldInfo.getTimeStamp();
			if (timeStamp == null) {
				// If the entry line has no timestamp, put the file timestamp in the entry line
				MutableResourceSyncInfo mutable = oldInfo.cloneMutable();
				mutable.setTimeStamp(getTimeStamp());
				// We report the modification change ourselves below
				mutable.reported();
				newInfo = mutable;
			} else {
				// reset the file timestamp to the one from the entry line
				setTimeStamp(timeStamp);
				// (newInfo = null) No need to set the newInfo as there is no sync info change
			}
			// (modified = false) the file will be no longer modified
		} else if (oldInfo == null) {
			// cvs add of a file
			newInfo = new ResourceSyncInfo(entryLine, null, null);
			// an added file should show up as modified
			modified = true;
		} else {
			// commit of a changed file
			newInfo = new ResourceSyncInfo(entryLine, oldInfo.getPermissions(), getTimeStamp());
			// (modified = false) a committed file is no longer modified
			
		}
		if (newInfo != null) setSyncInfo(newInfo);
		setModified(modified);
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
		run(new ICVSRunnable() {
			public void run(IProgressMonitor monitor) throws CVSException {
				EclipseFile.super.unmanage(monitor);
				clearCachedBase();
				// Reset the modified state of any unmanaged file
				setModified(false);
			}
		}, monitor);
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#isEdited()
	 */
	public boolean isEdited() throws CVSException {
		return EclipseSynchronizer.getInstance().isEdited(getIFile());
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSResource#setSyncInfo(org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo)
	 */
	public void setSyncInfo(ResourceSyncInfo info) throws CVSException {
		super.setSyncInfo(info);
		if (info.needsReporting()) {
			setModified(computeModified(info));
			info.reported();
		}
	}
	
	/**
	 * Method updated flags the objetc as having been modfied by the updated
	 * handler. This flag is read during the resource delta to determine whether
	 * the modification made the file dirty or not.
	 *
	 * @param mFile
	 */
	public void updated() throws CVSException {
		EclipseSynchronizer.getInstance().markFileAsUpdated(getIFile());
	}
	
	public boolean handleModification(boolean forAddition) throws CVSException {
		if (isIgnored()) {			
			// Special case handling for when a resource passes from the un-managed state
			// to the ignored state (e.g. ignoring the ignore file). Parent dirty state must be
			// recalculated but since the resource's end state is ignored there is a lot of code
			// in the plugin that simply disregards the change to the resource.
			// There may be a better was of handling resources that transition from un-managed to
			// ignored but for now this seems like the safest change. 
			if(! resource.isDerived()) {
				setModified(false);
			}
			return true;
		} 
		// set the modification state to what it really is and return true if the modification state changed
		setModified(computeModified(getSyncInfo()));
		return false;
	}
	
	/**
	 * Sets the modified status of the receiver. This method
	 * returns true if there was a change in the modified status of the file.
	 */
	private void setModified(boolean modified) throws CVSException {
		EclipseSynchronizer.getInstance().setDirtyIndicator(getIFile(), modified);
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.EclipseResource#run(org.eclipse.team.internal.ccvs.core.ICVSRunnable, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void run(ICVSRunnable job, IProgressMonitor monitor) throws CVSException {
		getParent().run(job, monitor);
	}
}


