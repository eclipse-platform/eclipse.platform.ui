/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.QuietOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.LogListener;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * This class provides the implementation of ICVSRemoteFile and IManagedFile for
 * use by the repository and sync view.
 */
public class RemoteFile extends RemoteResource implements ICVSRemoteFile  {

	// Contents will be cahced to disk when this thrshold is exceeded
	private static final int CACHING_THRESHOLD = 32768;
	
	// cache for file contents received from the server
	private byte[] contents;
	// cach the log entry for the remote file
	private ILogEntry entry;
			
	/**
	 * Static method which creates a file as a single child of its parent.
	 * This should only be used when one is only interested in the file alone.
	 * 
	 * The returned RemoteFile represents the base of the local resource.
	 * If the local resource does not have a base, then null is returned
	 * even if the resource does exists remotely (e.g. created by another party).
	 */
	public static RemoteFile getBase(RemoteFolder parent, ICVSFile managed) throws CVSException {
		ResourceSyncInfo info = managed.getSyncInfo();
		if ((info == null) || info.isAdded()) {
			// Either the file is unmanaged or has just been added (i.e. doesn't necessarily have a remote)
			return null;
		}
		RemoteFile file = new RemoteFile(parent, managed.getSyncInfo());
		parent.setChildren(new ICVSRemoteResource[] {file});
		return file;
	}
	
	/**
	 * Static method which creates a file as a single child of its parent.
	 * This should only be used when one is only interested in the file alone.
	 * 
	 * The returned RemoteFile represents the latest remote revision corresponding to the local resource.
	 * If the local resource does not have a base, then null is returned
	 * even if the resource does exists remotely (e.g. created by another party).
	 */
	public static RemoteFile getLatest(RemoteFolder parent, ICVSFile managed, CVSTag tag, IProgressMonitor monitor) throws CVSException {
		ResourceSyncInfo info = managed.getSyncInfo();
		if ((info == null) || info.isAdded()) {
			// Either the file is unmanaged or has just been added (i.e. doesn't necessarily have a remote)
			return null;
		}
		
		// ensure that the entry line has the tag in it. Or else status will return
		// the latest from the branch of the local resource.
		if(tag!=null) {
			MutableResourceSyncInfo newInfo = info.cloneMutable();
			newInfo.setTag(tag);
			info = newInfo;
		}
		
		// initialize with new sync info from the local resource, this info will
		// be updated when updateRevision is called.
		RemoteFile file = new RemoteFile(parent, info);
		
		// use the contents of the file on disk so that the server can calculate the relative
		// sync state. This is a trick to allow the server to calculate sync state for us.
		InputStream is = managed.getContents();
		file.setContents(is, ICVSFile.UPDATED, false, Policy.monitorFor(null));
			
		parent.setChildren(new ICVSRemoteResource[] {file});
		if( ! file.updateRevision(tag, monitor)) {
			// If updateRevision returns false then the resource no longer exists remotely
			return null;
		}
		
		// forget local contents. Remote contents will be fetched the next time
		// the returned handle is used.
		file.clearContents();
		return file;
	}
	
	/**
	 * Forget the contents associated with this remote handle.
	 */
	public void clearContents() {
		contents = null;
	}
		
	/**
	 * Constructor for RemoteFile that should be used when nothing is know about the
	 * file ahead of time.
	 */
	// XXX do we need the first two constructors?
	public RemoteFile(RemoteFolder parent, int workspaceSyncState, String name, CVSTag tag) {
		this(parent, workspaceSyncState, name, "", tag);  //$NON-NLS-1$
		MutableResourceSyncInfo newInfo = info.cloneMutable();
		newInfo.setAdded();
	}
	
	public RemoteFile(RemoteFolder parent, int workspaceSyncState, String name, String revision, CVSTag tag) {
		this(parent, workspaceSyncState, null);
		MutableResourceSyncInfo newInfo = new MutableResourceSyncInfo(name, revision);		
		newInfo.setKeywordMode(Command.KSUBST_TEXT_EXPAND);
		newInfo.setTag(tag);
		info = newInfo;
	}
	
	public RemoteFile(RemoteFolder parent, ResourceSyncInfo info) {
		this(parent, Update.STATE_NONE, info);
	}
	
	public RemoteFile(RemoteFolder parent, int workspaceSyncState, ResourceSyncInfo newInfo) {
		this.parent = parent;
		info = newInfo;
		setWorkspaceSyncState(workspaceSyncState);
	}

	/**
	 * @see ICVSResource#accept(IManagedVisitor)
	 */
	public void accept(ICVSResourceVisitor visitor) throws CVSException {
		visitor.visitFile(this);
	}

	/**
	 * @see ICVSRemoteFile#getContents()
	 */
	public InputStream getContents(IProgressMonitor monitor) throws CVSException {
		if (contents == null) {
			// First, check to see if there's a cached contents for the file
			try {
				try {
					File ioFile = CVSProviderPlugin.getPlugin().getCacheFileFor(getCacheRelativePath());
					if (ioFile.exists()) {
						return new BufferedInputStream(new FileInputStream(ioFile));
					}
				} catch (IOException e) {
					// Try to purge the cache and continue
					File ioFile = CVSProviderPlugin.getPlugin().getCacheFileFor(getCacheRelativePath());
					if (ioFile.exists()) {
						ioFile.delete();
					}
				}
			} catch (IOException e) {
				// We will end up here if we couldn't read or delete the cache file
				throw new CVSException(new CVSStatus(IStatus.ERROR, 0, Policy.bind("RemoteFile.errorRetrievingFromCache", e.getMessage()), e));//$NON-NLS-1$
			}

			monitor.beginTask(Policy.bind("RemoteFile.getContents"), 100);//$NON-NLS-1$
			Session.run(getRepository(), parent, false, new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					monitor.beginTask(null, 100);
					IStatus status = Command.UPDATE.execute(
						Command.NO_GLOBAL_OPTIONS,
						new LocalOption[] { 
							Update.makeTagOption(new CVSTag(info.getRevision(), CVSTag.VERSION)),
							Update.IGNORE_LOCAL_CHANGES },
						new ICVSResource[] { RemoteFile.this },
						null,
						Policy.subMonitorFor(monitor, 90));
					if (status.getCode() != CVSStatus.SERVER_ERROR) {
						try {
							getLogEntry(Policy.subMonitorFor(monitor, 10));
						} catch (CVSException e) {
							// Ignore the status of the log entry fetch. 
							// If it fails, the entry will still be null
						}
					}
					monitor.done();
					if (status.getCode() == CVSStatus.SERVER_ERROR) {
						throw new CVSServerException(status);
					}
				}
			}, Policy.subMonitorFor(monitor, 100));

			// If the update succeeded but no contents were retreived from the server
			// than we can assume that the remote file has no contents.
			if (contents == null) {
				// The above is true unless there is a cache file
				try {
					File ioFile = CVSProviderPlugin.getPlugin().getCacheFileFor(getCacheRelativePath());
					if (ioFile.exists()) {
						return new BufferedInputStream(new FileInputStream(ioFile));
					} else {
						contents = new byte[0];
					}
				} catch (IOException e) {
					// Something is wrong with the cached file so signal an error.
					throw new CVSException(new CVSStatus(IStatus.ERROR, 0, Policy.bind("RemoteFile.errorRetrievingFromCache", e.getMessage()), e));//$NON-NLS-1$
				}
			}
		}
		return new ByteArrayInputStream(contents);
	}
	
	/*
	 * @see ICVSRemoteFile#getLogEntry(IProgressMonitor)
	 */
	public ILogEntry getLogEntry(IProgressMonitor monitor) throws CVSException {
		if (entry == null) {
			Session.run(getRepository(), parent, false, new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					monitor = Policy.monitorFor(monitor);
					monitor.beginTask(Policy.bind("RemoteFile.getLogEntries"), 100); //$NON-NLS-1$
					try {
						final List entries = new ArrayList();
						IStatus status = Command.LOG.execute(
							Command.NO_GLOBAL_OPTIONS,
							new LocalOption[] { 
								Command.LOG.makeRevisionOption(info.getRevision())},
							new ICVSResource[] { RemoteFile.this },
							new LogListener(RemoteFile.this, entries),
							Policy.subMonitorFor(monitor, 100));
						if (entries.size() == 1) {
							entry = (ILogEntry)entries.get(0);
						}
						if (status.getCode() == CVSStatus.SERVER_ERROR) {
							throw new CVSServerException(status);
						}
					} finally {
						monitor.done();
					}
				}
			}, monitor);
		}
		return entry;
	}
	
	/**
	 * @see ICVSRemoteFile#getLogEntries()
	 */
	public ILogEntry[] getLogEntries(IProgressMonitor monitor) throws CVSException {
		final List entries = new ArrayList();
		Session.run(getRepository(), parent, false, new ICVSRunnable() {
			public void run(IProgressMonitor monitor) throws CVSException {
				monitor = Policy.monitorFor(monitor);
				monitor.beginTask(Policy.bind("RemoteFile.getLogEntries"), 100); //$NON-NLS-1$
				QuietOption quietness = CVSProviderPlugin.getPlugin().getQuietness();
				try {
					CVSProviderPlugin.getPlugin().setQuietness(Command.VERBOSE);
					IStatus status = Command.LOG.execute(Command.NO_GLOBAL_OPTIONS, Command.NO_LOCAL_OPTIONS,
						new ICVSResource[] { RemoteFile.this }, new LogListener(RemoteFile.this, entries),
						Policy.subMonitorFor(monitor, 100));
					if (status.getCode() == CVSStatus.SERVER_ERROR) {
						throw new CVSServerException(status);
					}
				} finally {
					CVSProviderPlugin.getPlugin().setQuietness(quietness);
					monitor.done();
				}
			}
		}, monitor);
		return (ILogEntry[])entries.toArray(new ILogEntry[entries.size()]);
	}
	
	/**
	 * @see ICVSRemoteFile#getRevision()
	 */
	public String getRevision() {
		return info.getRevision();
	}
	
	/*
	 * Get a different revision of the remote file.
	 * 
	 * We must also create a new parent since the child is accessed through the parent from within CVS commands.
	 * Therefore, we need a new parent so that we can fecth the contents of the remote file revision
	 */
	public RemoteFile toRevision(String revision) {
		RemoteFolder newParent = new RemoteFolder(null, parent.getRepository(), new Path(parent.getRepositoryRelativePath()), parent.getTag());
		RemoteFile file = new RemoteFile(newParent, getWorkspaceSyncState(), getName(), revision, CVSTag.DEFAULT);
		newParent.setChildren(new ICVSRemoteResource[] {file});
		return file;
	}
	
		/**
	 * @see IManagedFile#getSize()
	 */
	public long getSize() {
		return contents == null ? 0 : contents.length;
	}

	/**
	 * @see IManagedFile#getFileInfo()
	 */
	public ResourceSyncInfo getSyncInfo() {
		return info;
	}
 	
	/**
	 * @see ICVSResource#getRelativePath(ICVSFolder)
	 */
	public String getRelativePath(ICVSFolder ancestor) throws CVSException {
		String result = parent.getRelativePath(ancestor);
		if (result.length() == 0)
			return getName();
		else
			return result + Session.SERVER_SEPARATOR + getName();
	}
	
	/**
	 * @see ICVSResource#getRemoteLocation(ICVSFolder)
	 */
	public String getRemoteLocation(ICVSFolder stopSearching) throws CVSException {
		return parent.getRemoteLocation(stopSearching) + Session.SERVER_SEPARATOR + getName();
	}
	
	/**
	 * Get the remote path for the receiver relative to the repository location path
	 */
	public String getRepositoryRelativePath() {
		String parentPath = parent.getRepositoryRelativePath();
		return parentPath + Session.SERVER_SEPARATOR + getName();
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
	public void setSyncInfo(ResourceSyncInfo fileInfo) {
		info = fileInfo;
	}

	/**
	 * Set the revision for this remote file.
	 * 
	 * @param revision to associated with this remote file
	 */
	public void setRevision(String revision) {
		MutableResourceSyncInfo newInfo = getSyncInfo().cloneMutable();
		newInfo.setRevision(revision);
		info = newInfo;
	}		
	
	public InputStream getContents() throws CVSException {
		if (contents == null) {
			// Check for cached contents for the file
			try {
				File ioFile = CVSProviderPlugin.getPlugin().getCacheFileFor(getCacheRelativePath());
				if (ioFile.exists()) {
					return new BufferedInputStream(new FileInputStream(ioFile));
				}
			} catch (IOException e) {
				throw new CVSException(new CVSStatus(IStatus.ERROR, 0, Policy.bind("RemoteFile.errorRetrievingFromCache", e.getMessage()), e));//$NON-NLS-1$
			}
		}
		return new ByteArrayInputStream(contents == null ? new byte[0] : contents);
	}

	public void setContents(InputStream stream, int responseType, boolean keepLocalHistory, IProgressMonitor monitor) throws CVSException {
		try {
			try {
				byte[] buffer = new byte[1024];
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				OutputStream out = byteStream;
				int read;
				try {
					try {
						while ((read = stream.read(buffer)) >= 0) {
							Policy.checkCanceled(monitor);
							out.write(buffer, 0, read);
							// Detect when the file is getting too big to keep in memory
							// and switch to a caching strategy for the contents of the file
							if (out == byteStream && byteStream.size() > CACHING_THRESHOLD) {
								// Get the cache file to write to
								File ioFile = CVSProviderPlugin.getPlugin().getCacheFileFor(getCacheRelativePath());
								if ( ! ioFile.getParentFile().exists()) {
									ioFile.getParentFile().mkdirs();
								}
								// Switch streams
								byteStream.close();
								out = new BufferedOutputStream(new FileOutputStream(ioFile));
								// Write what we've read so far
								out.write(byteStream.toByteArray());
								// Continue looping until the whole file is read
							}
						}
					} finally {
						out.close();
					}
				} catch (IOException e) {
					// Make sure we don't leave the cache file around as it may not have the right contents
					if (byteStream != out) {
						File ioFile = CVSProviderPlugin.getPlugin().getCacheFileFor(getCacheRelativePath());
						if (ioFile.exists()) {
							ioFile.delete();
						}
					}
					throw e;
				}

				// Set the contents if we didn't cahce them
				if (out == byteStream) {
					contents = byteStream.toByteArray();
				}
			} finally {
				stream.close();
			}
		} catch(IOException e) {
			throw CVSException.wrapException(e);
		}
	}
 
	/*
	 * @see ICVSFile#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) throws CVSException {
 	}

	/*
	 * @see ICVSFile#isReadOnly()
	 */
	public boolean isReadOnly() throws CVSException {
		return true;
	}
	
	/*
	 * @see ICVSFile#getTimeStamp()
	 */
	public Date getTimeStamp() {
		return info.getTimeStamp();
	}

	/*
	 * @see ICVSFile#setTimeStamp(Date)
	 */
	public void setTimeStamp(Date date) throws CVSException {
	}

	/*
	 * @see ICVSFile#isDirty()
	 */
	public boolean isDirty() throws CVSException {
		return false;
	}
	
	public boolean isModified() throws CVSException {
		// it is safe to always consider a remote file handle as modified. This will cause any
		// CVS command to fetch new contents from the server.
		return true;
	}

	/**
	 * @see IManagedFile#moveTo(IManagedFile)
	 */
	public void copyTo(String mFile) throws CVSException, ClassCastException {		
		// Do nothing
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
	 * @see ICVSResource#isFolder()
	 */
	public boolean isFolder() {
		return false;
	}
	
	public boolean updateRevision(CVSTag tag, IProgressMonitor monitor) throws CVSException {
		return parent.updateRevision(this, tag, monitor);
	}
	
	public boolean equals(Object target) {
		if (this == target)
			return true;
		if (!(target instanceof RemoteFile))
			return false;
		RemoteFile remote = (RemoteFile) target;
		return super.equals(target) && remote.getRevision().equals(getRevision());
	}
	
	/* 
	 * Return the cache relative path for the receiver as
	 *   host/cvs/root/module/path/.#filename revision
	 */
	private String getCacheRelativePath() {
		ICVSRepositoryLocation location = getRepository();
		IPath path = new Path(location.getHost());
		path = path.append(location.getRootDirectory());
		path = path.append(parent.getRepositoryRelativePath());
		path = path.append(getName() + ' ' + getRevision());
		return path.toString();
	}
}