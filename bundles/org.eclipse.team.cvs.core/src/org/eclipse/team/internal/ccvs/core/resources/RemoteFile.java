/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.resources;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.RemoteContentsCache;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.client.Log;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.QuietOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.LogListener;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.NotifyInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * This class provides the implementation of ICVSRemoteFile and IManagedFile for
 * use by the repository and sync view.
 */
public class RemoteFile extends RemoteResource implements ICVSRemoteFile, IStorage  {
	
	// sync info in byte form
	private byte[] syncBytes;
	// buffer for file contents received from the server
	private byte[] contents;
	// cache the log entry for the remote file
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
		byte[] syncBytes = managed.getSyncBytes();
		if ((syncBytes == null) || ResourceSyncInfo.isAddition(syncBytes)) {
			// Either the file is unmanaged or has just been added (i.e. doesn't necessarily have a remote)
			return null;
		}
		RemoteFile file = new RemoteFile(parent, syncBytes);
		parent.setChildren(new ICVSRemoteResource[] {file});
		return file;
	}
	
	public static RemoteFile fromBytes(IResource local, byte[] bytes, byte[] parentBytes) throws CVSException {
		Assert.isNotNull(bytes);
		Assert.isTrue(local.getType() == IResource.FILE);
		RemoteFolder parent = RemoteFolder.fromBytes(local.getParent(), parentBytes);
		RemoteFile file = new RemoteFile(parent, bytes);
		parent.setChildren(new ICVSRemoteResource[] {file});
		return file;
	}
	
	public static RemoteFile getRemote(IFile local, byte[] bytes) throws CVSException {
		RemoteFolder parent = (RemoteFolder)CVSWorkspaceRoot.getRemoteResourceFor(local.getParent());
		RemoteFile file = new RemoteFile(parent, bytes);
		parent.setChildren(new ICVSRemoteResource[] {file});
		return file;
	}
	
	/**
	 * Constructor for RemoteFile that should be used when nothing is know about the
	 * file ahead of time.
	 * @param parent the folder that is the parent of the file
	 * @param workspaceSyncState the workspace state (use Update.STATE_NONE if unknown)
	 * @param name the name of the file
	 * @param revision revision of the file or <code>null</code> if the revision is not known
	 * @param keywordMode keyword mode of the file or <code>null</code> if the mode is not known
	 * @param tag tag for the file
	 */
	public RemoteFile(RemoteFolder parent, int workspaceSyncState, String name, String revision, KSubstOption keywordMode, CVSTag tag) {
		this(parent, name, workspaceSyncState, getSyncBytes(name, revision, keywordMode, tag));
	}
	
	private static byte[] getSyncBytes(String name, String revision, KSubstOption keywordMode, CVSTag tag) {
		if (revision == null) {
			revision = ResourceSyncInfo.ADDED_REVISION;
		}
		if (keywordMode == null) {
			keywordMode = KSubstOption.getDefaultTextMode();
		}
		MutableResourceSyncInfo newInfo = new MutableResourceSyncInfo(name, revision);		
		newInfo.setKeywordMode(keywordMode);
		newInfo.setTag(tag);
		return newInfo.getBytes();
	}
	
	/* package */ RemoteFile(RemoteFolder parent, byte[] syncBytes) throws CVSException {
		this(parent, Update.STATE_NONE, syncBytes);
	}
	
	/* package */ RemoteFile(RemoteFolder parent, int workspaceSyncState, byte[] syncBytes) throws CVSException {
		this(parent, ResourceSyncInfo.getName(syncBytes), workspaceSyncState, syncBytes);
	}

	private RemoteFile(RemoteFolder parent, String name, int workspaceSyncState, byte[] syncBytes) {
		super(parent, name);
		this.syncBytes = syncBytes;
		setWorkspaceSyncState(workspaceSyncState);
	}

	/**
	 * @see ICVSResource#accept(ICVSResourceVisitor)
	 */
	public void accept(ICVSResourceVisitor visitor) throws CVSException {
		visitor.visitFile(this);
	}

	/**
	 * @see ICVSResource#accept(ICVSResourceVisitor, boolean)
	 */
	public void accept(ICVSResourceVisitor visitor, boolean recurse) throws CVSException {
		visitor.visitFile(this);
	}
	
	/**
	 * @see ICVSRemoteFile#getContents()
	 */
	public InputStream getContents(IProgressMonitor monitor) throws CVSException {
		if (contents == null) {
			// First, check to see if there's a cached contents for the file
			InputStream cached = getCachedContents();
			if (cached != null) {
				return cached;
			}
		
			// No  contents cached so fetch contents from the server.
			fetchContents(monitor);

			// If the update succeeded but no contents were retreived from the server
			// than we can assume that the remote file has no contents.
			if (contents == null) {
				// The above is true unless there is a cache file
				cached = getCachedContents();
				if (cached != null) {
					return cached;
				} else {
					contents = new byte[0];
				}
			}
		}
		return new ByteArrayInputStream(contents);
	}
	
	/* package*/ void fetchContents(IProgressMonitor monitor) throws CVSException {
		monitor.beginTask(Policy.bind("RemoteFile.getContents"), 100);//$NON-NLS-1$
		Session session = new Session(getRepository(), parent, false /* create backups */);
		session.open(Policy.subMonitorFor(monitor, 10), false /* read-only */);
		try {
			IStatus status = Command.UPDATE.execute(
				session,
				Command.NO_GLOBAL_OPTIONS,
				new LocalOption[] { 
					Update.makeTagOption(new CVSTag(getRevision(), CVSTag.VERSION)),
					Update.IGNORE_LOCAL_CHANGES },
				new ICVSResource[] { this },
				null,
				Policy.subMonitorFor(monitor, 90));
			if (status.getCode() == CVSStatus.SERVER_ERROR) {
				throw new CVSServerException(status);
			}
		} finally {
			session.close();
			monitor.done();
		}
	}

	/*
	 * @see ICVSRemoteFile#getLogEntry(IProgressMonitor)
	 */
	public ILogEntry getLogEntry(IProgressMonitor monitor) throws CVSException {
		if (entry == null) {
			monitor = Policy.monitorFor(monitor);
			monitor.beginTask(Policy.bind("RemoteFile.getLogEntries"), 100); //$NON-NLS-1$
			Session session = new Session(getRepository(), parent, false /* output to console */);
			session.open(Policy.subMonitorFor(monitor, 10), false /* read-only */);
			try {
				try {
					final List entries = new ArrayList();
					IStatus status = Command.LOG.execute(
						session,
						Command.NO_GLOBAL_OPTIONS,
						new LocalOption[] { 
							Log.makeRevisionOption(getRevision())},
						new ICVSResource[] { RemoteFile.this },
						new LogListener(RemoteFile.this, entries),
						Policy.subMonitorFor(monitor, 90));
					if (entries.size() == 1) {
						entry = (ILogEntry)entries.get(0);
					}
					if (status.getCode() == CVSStatus.SERVER_ERROR) {
						throw new CVSServerException(status);
					}
				} finally {
					monitor.done();
				}
			} finally {
				session.close();
			}
		}
		return entry;
	}
	
	/**
	 * @see ICVSRemoteFile#getLogEntries()
	 */
	public ILogEntry[] getLogEntries(IProgressMonitor monitor) throws CVSException {
		monitor = Policy.monitorFor(monitor);
		monitor.beginTask(Policy.bind("RemoteFile.getLogEntries"), 100); //$NON-NLS-1$
		final List entries = new ArrayList();
		Session session = new Session(getRepository(), parent, false /* output to console */);
		session.open(Policy.subMonitorFor(monitor, 10), false /* read-only */);
		try {
			QuietOption quietness = CVSProviderPlugin.getPlugin().getQuietness();
			try {
				CVSProviderPlugin.getPlugin().setQuietness(Command.VERBOSE);
				IStatus status = Command.LOG.execute(
					session,
					Command.NO_GLOBAL_OPTIONS, Command.NO_LOCAL_OPTIONS,
					new ICVSResource[] { RemoteFile.this }, new LogListener(RemoteFile.this, entries),
					Policy.subMonitorFor(monitor, 90));
				if (status.getCode() == CVSStatus.SERVER_ERROR) {
					throw new CVSServerException(status);
				}
			} finally {
				CVSProviderPlugin.getPlugin().setQuietness(quietness);
				monitor.done();
			}
		} finally { 
			session.close();
		}
		return (ILogEntry[])entries.toArray(new ILogEntry[entries.size()]);
	}
	
	/**
	 * @see ICVSRemoteFile#getRevision()
	 */
	public String getRevision() {
		try {
			return ResourceSyncInfo.getRevision(syncBytes);
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
			return ResourceSyncInfo.ADDED_REVISION;
		}
	}
	
	private KSubstOption getKeywordMode() {
		try {
			return ResourceSyncInfo.getKeywordMode(syncBytes);
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
			return KSubstOption.getDefaultTextMode();
		}
	}
	
	/*
	 * Get a different revision of the remote file.
	 * 
	 * We must also create a new parent since the child is accessed through the parent from within CVS commands.
	 * Therefore, we need a new parent so that we can fecth the contents of the remote file revision
	 */
	public RemoteFile toRevision(String revision) {
		RemoteFolder newParent = new RemoteFolder(null, parent.getRepository(), parent.getRepositoryRelativePath(), parent.getTag());
		RemoteFile file = new RemoteFile(newParent, getWorkspaceSyncState(), getName(), revision, getKeywordMode(), CVSTag.DEFAULT);
		newParent.setChildren(new ICVSRemoteResource[] {file});
		return file;
	}

	/**
	 * @see ICVSFile#getSize()
	 */
	public long getSize() {
		if (contents == null) {
			File ioFile = getRemoteContentsCache().getFile(getCacheRelativePath());
			if (ioFile.exists()) {
				return ioFile.length();
			}
		}
		return contents == null ? 0 : contents.length;
	}

	/**
	 * @see ICVSFile#getSyncInfo()
	 */
	public ResourceSyncInfo getSyncInfo() {
		try {
			return new ResourceSyncInfo(syncBytes);
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
			return null;
		}
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
	public void setSyncInfo(ResourceSyncInfo fileInfo, int modificationState) {
		syncBytes = fileInfo.getBytes();
	}

	/**
	 * Set the revision for this remote file.
	 * 
	 * @param revision to associated with this remote file
	 */
	public void setRevision(String revision) throws CVSException {
		syncBytes = ResourceSyncInfo.setRevision(syncBytes, revision);
	}		
	
	public InputStream getContents() throws CVSException {
		if (contents == null) {
			// Check for cached contents for the file
			InputStream cached = getCachedContents();
			if (cached != null) {
				return cached;
			}
		}
		return new ByteArrayInputStream(contents == null ? new byte[0] : contents);
	}

	private InputStream getCachedContents() throws CVSException {
		try {
			return getRemoteContentsCache().getContents(getCacheRelativePath());
		} catch (TeamException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	public void setContents(InputStream stream, int responseType, boolean keepLocalHistory, IProgressMonitor monitor) throws CVSException {
		try {
			getRemoteContentsCache().setContents(getCacheRelativePath(), stream, monitor);
		} catch (TeamException e) {
			throw CVSException.wrapException(e);
		}
	}
 
	private RemoteContentsCache getRemoteContentsCache() {
		return RemoteContentsCache.getCache(CVSProviderPlugin.ID);
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
	
	/*
	 * Return whether there are already contents cached for the given handle
	 */
	public boolean isContentsCached() {
		return getRemoteContentsCache().hasContents(getCacheRelativePath());
	}
	
	/*
	 * @see ICVSFile#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) throws CVSException {
 	}

	/*
	 * @see ICVSFile#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}
	
	/*
	 * @see ICVSFile#getTimeStamp()
	 */
	public Date getTimeStamp() {
		return getSyncInfo().getTimeStamp();
	}

	/*
	 * @see ICVSFile#setTimeStamp(Date)
	 */
	public void setTimeStamp(Date date) throws CVSException {
	}

	/**
	 * @see ICVSFile#moveTo(String)
	 */
	public void copyTo(String mFile) throws CVSException {		
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
	
	/*
	 * @see ICVSResource#tag(CVSTag, LocalOption[], IProgressMonitor)
	 * 
	 * The revision of the remote file is used as the base for the tagging operation
	 */
	 public IStatus tag(final CVSTag tag, final LocalOption[] localOptions, IProgressMonitor monitor) throws CVSException {
		monitor = Policy.monitorFor(monitor);
		monitor.beginTask(null, 100);
		Session session = new Session(getRepository(), getParent(), true /* output to console */);
		session.open(Policy.subMonitorFor(monitor, 10), true /* open for modification */);
		try {
			return Command.RTAG.execute(
				session,
				Command.NO_GLOBAL_OPTIONS,
				localOptions,
				new CVSTag(getRevision(), CVSTag.VERSION),
				tag,
				new ICVSRemoteResource[] { RemoteFile.this },
			Policy.subMonitorFor(monitor, 90));
		} finally {
			session.close();
		}
	 }
	
	public boolean equals(Object target) {
		if (this == target)
			return true;
		if (!(target instanceof RemoteFile))
			return false;
		RemoteFile remote = (RemoteFile) target;
		return super.equals(target) && remote.getRevision().equals(getRevision());
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#checkout(int)
	 */
	public void edit(int notifications, IProgressMonitor monitor) throws CVSException {
		// do nothing
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#uncheckout()
	 */
	public void unedit(IProgressMonitor monitor) throws CVSException {
		// do nothing
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#notificationCompleted()
	 */
	public void notificationCompleted() {
		// do nothing
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#getPendingNotification()
	 */
	public NotifyInfo getPendingNotification() throws CVSException {
		return null;
	}

	/**
	 * @see RemoteResource#forTag(ICVSRemoteFolder, CVSTag)
	 */
	public ICVSRemoteResource forTag(ICVSRemoteFolder parent, CVSTag tag) {
		return new RemoteFile((RemoteFolder)parent, getWorkspaceSyncState(), getName(), getRevision(), getKeywordMode(), tag);
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSRemoteResource#forTag(org.eclipse.team.internal.ccvs.core.CVSTag)
	 */
	public ICVSRemoteResource forTag(CVSTag tag) {
		RemoteFolderTree remoteFolder = new RemoteFolderTree(null, getRepository(), 
			((ICVSRemoteFolder)getParent()).getRepositoryRelativePath(), 
			tag);
		RemoteFile remoteFile = (RemoteFile)forTag(remoteFolder, tag);
		remoteFolder.setChildren(new ICVSRemoteResource[] { remoteFile });
		return remoteFile;
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#committed(org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo)
	 */
	public void checkedIn(String info) throws CVSException {
		// do nothing
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#isEdited()
	 */
	public boolean isEdited() throws CVSException {
		return false;
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#getSyncBytes()
	 */
	public byte[] getSyncBytes() {
		return syncBytes;
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#setSyncBytes(byte[])
	 */
	public void setSyncBytes(byte[] syncBytes, int modificationState) throws CVSException {
		setSyncInfo(new ResourceSyncInfo(syncBytes), ICVSFile.UNKNOWN);
	}

	public String toString() {
		return super.toString() + " " + getRevision(); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.IRemoteResource#getComment()
	 */
	public String getComment() throws CVSException {
		ILogEntry entry = getLogEntry(new NullProgressMonitor());
		return entry.getComment();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.IRemoteResource#getContentIdentifier()
	 */
	public String getContentIdentifier() throws CVSException {
		return getRevision();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.IRemoteResource#getCreatorDisplayName()
	 */
	public String getCreatorDisplayName() throws CVSException {
		ILogEntry entry = getLogEntry(new NullProgressMonitor());
		return entry.getAuthor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.IRemoteResource#getBufferedStorage(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStorage getBufferedStorage(IProgressMonitor monitor) throws TeamException {
		// Invoke getContents which ensures that contents are cached
		getContents(monitor);
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getFullPath()
	 */
	public IPath getFullPath() {
		return new Path(getRepositoryRelativePath());
	}
}
