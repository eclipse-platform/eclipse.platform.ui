package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSStatus;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.ccvs.core.ICVSRunnable;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.QuietOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.IStatusListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.IUpdateMessageListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.StatusListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.UpdateListener;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * This class provides the implementation of ICVSRemoteFolder
 * 
 * The parent of the RemoteFolder represents the folders parent in a local configuration.
 * For instance, the parent may correspond to the remote parent or may be a folder in the
 * same repository that has no physical relationship to the RemoteFolder (resulting from the use
 * of a module definition, for instance). A RemoteFolder may not have a parent, indicating that it is
 * the root of the local configuration it represents. 
 * 
 * A RemoteFolder has the following:
 *   A name in the folder's local configuration
 *   
 */
public class RemoteFolder extends RemoteResource implements ICVSRemoteFolder, ICVSFolder {
	
	protected FolderSyncInfo folderInfo;
	private ICVSRemoteResource[] children;
	private ICVSRepositoryLocation repository;
	
	/**
	 * Constructor for RemoteFolder.
	 */
	public RemoteFolder(RemoteFolder parent, ICVSRepositoryLocation repository, IPath repositoryRelativePath, CVSTag tag) {
		this(parent, 
			repositoryRelativePath.lastSegment() == null ? "" : repositoryRelativePath.lastSegment(), //$NON-NLS-1$
			repository,
			repositoryRelativePath,
			tag, 
			false);	
	}
	
	public RemoteFolder(RemoteFolder parent, String name, ICVSRepositoryLocation repository, IPath repositoryRelativePath, CVSTag tag, boolean isStatic) {
		this.info = new ResourceSyncInfo(name);
		this.parent = parent;
		this.folderInfo = new FolderSyncInfo(repositoryRelativePath.toString(), repository.getLocation(), tag, isStatic);
		this.repository = repository;	
	}

	// Get the file revisions for the given filenames
	protected void updateFileRevisions(Session session, String[] fileNames, IProgressMonitor monitor) throws CVSException {
		
		final int[] count = new int[] {0};
		
		// Create a listener for receiving the revision info
		IStatusListener listener = new IStatusListener() {
			public void fileStatus(IPath path, String remoteRevision) {
				if (remoteRevision == IStatusListener.FOLDER_REVISION)
					// Ignore any folders
					return;
				try {
					((RemoteFile)getChild(path.lastSegment())).setRevision(remoteRevision);
					count[0]++;
				} catch (CVSException e) {
					// The count wil be off to indicate an error
				}
			}
		};
			
		// Perform a "cvs status..." with a listener
		QuietOption quietness = CVSProviderPlugin.getPlugin().getQuietness();
		try {
			CVSProviderPlugin.getPlugin().setQuietness(Command.VERBOSE);
			IStatus status = Command.STATUS.execute(session,
				Command.NO_GLOBAL_OPTIONS,
				Command.NO_LOCAL_OPTIONS,
				fileNames,
				new StatusListener(listener),
				monitor);
			if (status.getCode() == CVSStatus.SERVER_ERROR) {
				throw new CVSServerException(status);
			}
		} finally {
			CVSProviderPlugin.getPlugin().setQuietness(quietness);
		}
			
		if (count[0] != fileNames.length)
			throw new CVSException(Policy.bind("RemoteFolder.errorFetchingRevisions")); //$NON-NLS-1$
	}
	
	/**
	 * @see IManagedResource#accept(IManagedVisitor)
	 */
	public void accept(ICVSResourceVisitor visitor) throws CVSException {
		visitor.visitFolder(this);
	}

	/*
	 * @see ICVSRemoteResource#exists(IProgressMonitor)
	 */
	public boolean exists(IProgressMonitor monitor) throws TeamException {
		try {
			members(monitor);
			return true;
		} catch (CVSException e) {
			if (e.getStatus().getCode() == CVSStatus.DOES_NOT_EXISTS) {
				return false;
			} else {
				throw e;
			}
		}
	}

	/*
	 * Check whether the given resource is a child of the receiver remotely
	 */
	protected boolean exists(ICVSRemoteResource child, IProgressMonitor monitor) throws CVSException {
		return exists(child, getTag(), monitor);
	}
	
	/*
	 * Check whether the child exists for the given tag. This additional method is required because
	 * CVS will signal an error if a folder only contains subfolders when a tag is used. If we get this
	 * error and we're looking for a folder, we need to reissue the command without a tag.
	 */
	protected boolean exists(ICVSRemoteResource child, CVSTag tag, IProgressMonitor monitor) throws CVSException {
		
		final IProgressMonitor progress = Policy.monitorFor(monitor);
		
		// Create the listener for remote files and folders
		final boolean[] exists = new boolean[] {false};
		IUpdateMessageListener listener = new IUpdateMessageListener() {
			public void directoryInformation(IPath path, boolean newDirectory) {
				exists[0] = true;
			}
			public void directoryDoesNotExist(IPath path) {
			}
			public void fileInformation(int type, String filename) {
				exists[0] = true;
			}
			public void fileDoesNotExist(String filename) {
			}
		};
		
		// Build the local options
		List localOptions = new ArrayList();
		localOptions.add(Update.RETRIEVE_ABSENT_DIRECTORIES);
		if (tag != null && tag.getType() != CVSTag.HEAD)
			localOptions.add(Update.makeTagOption(tag));
		
		// Retrieve the children and any file revision numbers in a single connection
		// Perform a "cvs -n update -d -r tagName folderName" with custom message and error handlers		
		IStatus status;
		Session s = new Session(getRepository(), this, false);
		s.open(monitor);
		try {
			status = Command.UPDATE.execute(s,
			new GlobalOption[] { Command.DO_NOT_CHANGE },
			(LocalOption[]) localOptions.toArray(new LocalOption[localOptions.size()]), 
			new String[] { child.getName() },
			new UpdateListener(listener),
			monitor);
		} finally {
			s.close();
		}
		if (status.getCode() == CVSStatus.SERVER_ERROR) {
			CVSServerException e = new CVSServerException(status);
			if ( ! e.isNoTagException() || ! child.isContainer())
				if (e.containsErrors())
					throw e;
			// we now know that this is an exception caused by a cvs bug.
			// if the folder has no files in it (just subfolders) cvs does not respond with the subfolders...
			// workaround: retry the request with no tag to get the directory names (if any)
			Policy.checkCanceled(progress);
			return exists(child, null, progress);
		}
		return exists[0];
	}

	/**
	 * @see ICVSRemoteFolder#getMembers()
	 */
	public ICVSRemoteResource[] getMembers(IProgressMonitor monitor) throws TeamException {
		return getMembers(getTag(), monitor);
	}

	/**
	 * This method gets the members for a given tag and returns them.
	 * During the execution of this method, the instance variable children
	 * will be used to contain the children. However, the variable is reset
	 * and the result returned. Thus, instances of RemoteFolder do not
	 * persist the children. Subclasses (namely RemoteFolderTree) may
	 * persist the children.
	 */
	protected ICVSRemoteResource[] getMembers(final CVSTag tag, IProgressMonitor monitor) throws TeamException {
		
		final IProgressMonitor progress = Policy.monitorFor(monitor);
		
		// Forget about any children we used to know about children
		children = null;
		
		// Create the listener for remote files and folders
		final List newRemoteDirectories = new ArrayList();
		final List newRemoteFiles = new ArrayList();
		final boolean[] exists = new boolean[] {true};
		IUpdateMessageListener listener = new IUpdateMessageListener() {
			public void directoryInformation(IPath path, boolean newDirectory) {
				if (newDirectory && path.segmentCount() == 1) {
					newRemoteDirectories.add(path.lastSegment());
					progress.subTask(path.lastSegment().toString());
					progress.worked(1);
				}
			}
			public void directoryDoesNotExist(IPath path) {
				if (path.isEmpty()) {
					// the remote folder doesn't exist
					exists[0] = false;
				}
			}
			public void fileInformation(int type, String filename) {
				IPath filePath = new Path(filename);	
				if( filePath.segmentCount() == 1 ) {
					String properFilename = filePath.lastSegment();
					newRemoteFiles.add(properFilename);
					progress.subTask(properFilename);
					progress.worked(1);
				}
			}
			public void fileDoesNotExist(String filename) {
			}
		};
		
		// Build the local options
		List localOptions = new ArrayList();
		localOptions.add(Update.RETRIEVE_ABSENT_DIRECTORIES);
		if (tag != null) localOptions.add(Update.makeTagOption(tag));
		
		// Retrieve the children and any file revision numbers in a single connection
		Session s = new Session(getRepository(), this, false);
		s.open(monitor);
		try {
			// Perform a "cvs -n update -d -r tagName folderName"
			IStatus status = Command.UPDATE.execute(s,
				new GlobalOption[] { Command.DO_NOT_CHANGE },
				(LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]),
				new String[] { Session.CURRENT_LOCAL_FOLDER },
				new UpdateListener(listener),
				monitor);
			if (status.getCode() == CVSStatus.SERVER_ERROR) {
				throw new CVSServerException(status);
			}
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
			if (! exists[0]) {
				throw new CVSException(new CVSStatus(CVSStatus.ERROR, CVSStatus.DOES_NOT_EXISTS, Policy.bind("RemoteFolder.doesNotExist", getRepositoryRelativePath())));
			}
			
			// Convert the file and folder names to IManagedResources
			List result = new ArrayList();
			for (int i=0;i<newRemoteFiles.size();i++) {
				result.add(new RemoteFile(this, Update.STATE_NONE, (String)newRemoteFiles.get(i), tag));
			}
			for (int i=0;i<newRemoteDirectories.size();i++)
				result.add(new RemoteFolder(this, getRepository(), new Path(getRepositoryRelativePath()).append((String)newRemoteDirectories.get(i)), tag));
			children = (ICVSRemoteResource[])result.toArray(new ICVSRemoteResource[0]);

			// Get the revision numbers for the files
			if (newRemoteFiles.size() > 0) {
				updateFileRevisions(s, (String[])newRemoteFiles.toArray(new String[newRemoteFiles.size()]), monitor);
			}
			
		} catch (CVSServerException e) {
			if ( ! e.isNoTagException() && e.containsErrors())
				throw e;
			if (tag == null)
				throw e;
			// we now know that this is an exception caused by a cvs bug.
			// if the folder has no files in it (just subfolders) cvs does not respond with the subfolders...
			// workaround: retry the request with no tag to get the directory names (if any)
			Policy.checkCanceled(progress);
			children = getMembers(null, progress);
			// the returned children must be given the original tag
			for (int i = 0; i < children.length; i++) {
				ICVSRemoteResource remoteResource = children[i];
				if(remoteResource.isContainer()) {
					((RemoteFolder)remoteResource).setTag(tag);
				}
			}
		} finally {
			s.close();
		}

		// We need to remember the children that were fetched in order to support file
		// operations that depend on the parent knowing about the child (i.e. RemoteFile#getContents)
		return children;
	}

	/**
	 * @see ICVSFolder#getFolders()
	 */
	public ICVSFolder[] getFolders() throws CVSException {
		ICVSRemoteResource[] children = getChildren();
		if (children == null)
			return new ICVSFolder[0];
		else {
			List result = new ArrayList();
			for (int i=0;i<children.length;i++)
				if (((ICVSResource)children[i]).isFolder())
					result.add(children[i]);
			return (ICVSFolder[])result.toArray(new ICVSFolder[result.size()]);
		}
	}

	/**
	 * @see ICVSFolder#getFiles()
	 */
	public ICVSFile[] getFiles() throws CVSException {
		ICVSRemoteResource[] children = getChildren();
		if (children == null)
			return new ICVSFile[0];
		else {
			List result = new ArrayList();
			for (int i=0;i<children.length;i++)
				if (!((ICVSResource)children[i]).isFolder())
					result.add(children[i]);
			return (ICVSFile[])result.toArray(new ICVSFile[result.size()]);
		}
	}

	/**
	 * @see ICVSFolder#getFolder(String)
	 */
	public ICVSFolder getFolder(String name) throws CVSException {
		if (name.equals(Session.CURRENT_LOCAL_FOLDER) || name.equals(Session.CURRENT_LOCAL_FOLDER + Session.SERVER_SEPARATOR))
			return this;
		ICVSResource child = getChild(name);
		if (child.isFolder())
			return (ICVSFolder)child;
		throw new CVSException(Policy.bind("RemoteFolder.invalidChild", new Object[] {name})); //$NON-NLS-1$
	}

	/**
	 * @see ICVSFolder#getFile(String)
	 */
	public ICVSFile getFile(String name) throws CVSException {
		ICVSResource child = getChild(name);
		if (!child.isFolder())
			return (ICVSFile)child;
		throw new CVSException(Policy.bind("RemoteFolder.invalidChild", new Object[] {name})); //$NON-NLS-1$

	}

	public LocalOption[] getLocalOptions() {
		return Command.NO_LOCAL_OPTIONS;
	}
	
	protected String getLocalPath() {
		throw new UnsupportedOperationException();
	}
	
	public String getRepositoryRelativePath() {
		// The REPOSITORY property of the folder info is the repository relative path
		return getFolderSyncInfo().getRepository();
	}
	
	/**
	 * @see ICVSResource#getRelativePath(ICVSFolder)
	 */
	public String getRelativePath(ICVSFolder ancestor) throws CVSException {
		
		if (ancestor == this)
			return Session.CURRENT_LOCAL_FOLDER;

		RemoteResource rootFolder;
		try {
			rootFolder = (RemoteResource)ancestor;
		} catch (ClassCastException e) {
			throw new CVSException(Policy.bind("RemoteResource.invalidResourceClass"),e); //$NON-NLS-1$
		}
		
		return Util.getRelativePath(rootFolder.getLocalPath(), getLocalPath());	
	}
	
	public ICVSRepositoryLocation getRepository() {
		return repository;
	}
	
	/**
	 * @see ICVSRemoteFolder#isExpandable()
	 */
	public boolean isExpandable() {
		return true;
	}
	
	/**
	 * @see ICVSResource#isFolder()
	 */
	public boolean isFolder() {
		return true;
	}
	
	/**
	 * @see ICVSFolder#childExists(String)
	 */
	public boolean childExists(String path) {
		try {
			return getChild(path) != null;
		} catch (CVSException e) {
			return false;
		}
	}

	/**
	 * @see ICVSFolder#getChild(String)
	 * 
	 * XXX: shouldn't this consider the case where children is null. Maybe
	 * by running the update + status with only one member?
	 * 
	 * XXX: The only problem with the above is that this is not supposed to be a long 
	 * running method. Also, path could be a file or folder  and can be more than one level.
	 * 
	 * This getChild is geared to work with the Command hierarchy. Therefore it only returns 
	 * children that were previously fetched by a call to getMembers(). If the request child
	 * does not exist, an exception is thrown.
	 */
	public ICVSResource getChild(String path) throws CVSException {
		if (path.equals(Session.CURRENT_LOCAL_FOLDER))
			return this;
		ICVSRemoteResource[] children = getChildren();
		if (children == null) 
			throw new CVSException(Policy.bind("RemoteFolder.invalidChild", path, getName()));//$NON-NLS-1$
		if (path.indexOf(Session.SERVER_SEPARATOR) == -1) {
			for (int i=0;i<children.length;i++) {
				if (children[i].getName().equals(path))
					return (ICVSResource)children[i];
			}
		} else {
			IPath p = new Path(path);
			try {
				return ((RemoteFolder)getChild(p.segment(0))).getChild(p.removeFirstSegments(1).toString());
			} catch (CVSException e) {
				// regenerate the exception to give as much info as possible
				throw new CVSException(Policy.bind("RemoteFolder.invalidChild", path, getName()));//$NON-NLS-1$
			}
		}
		throw new CVSException(Policy.bind("RemoteFolder.invalidChild", path, getName()));//$NON-NLS-1$
	}

	/**
	 * @see ICVSFolder#mkdir()
	 */
	public void mkdir() throws CVSException {
		throw new CVSException(Policy.bind("RemoteResource.invalidOperation"));//$NON-NLS-1$
	}

	/**
	 * @see ICVSFolder#flush(boolean)
	 */
	public void flush(boolean deep) {
	}

	/**
	 * @see ICVSFolder#getFolderInfo()
	 */
	public FolderSyncInfo getFolderSyncInfo() {
		return folderInfo;
	}

	/**
	 * @see ICVSResource#getRemoteLocation(ICVSFolder)
	 */
	public String getRemoteLocation(ICVSFolder stopSearching) throws CVSException {
		return folderInfo.getRemoteLocation();
	}
	
	/**
	 * @see ICVSFolder#isCVSFolder()
	 */
	public boolean isCVSFolder() {
		return true;
	}

	/**
	 * @see ICVSFolder#acceptChildren(ICVSResourceVisitor)
	 */
	public void acceptChildren(ICVSResourceVisitor visitor) throws CVSException {
		throw new CVSException(Policy.bind("RemoteResource.invalidOperation"));//$NON-NLS-1$
	}
	
	/*
	 * @see IRemoteResource#isContainer()
	 */
	public boolean isContainer() {
		return true;
	}
	
	/*
	 * @see IRemoteResource#members(IProgressMonitor)
	 */
	public IRemoteResource[] members(IProgressMonitor progress) throws TeamException {
		return getMembers(progress);
	}

	/*
	 * @see IRemoteResource#getContents(IProgressMonitor)
	 */
	public InputStream getContents(IProgressMonitor progress) throws TeamException {
		return null;
	}

	/*
	 * Answers the immediate cached children of this remote folder or null if the remote folder
	 * handle has not yet queried the server for the its children.
	 */	
	public ICVSRemoteResource[] getChildren() {
		return children;
	}
	/*
	 * This allows subclass to set the children
	 */
	protected void setChildren(ICVSRemoteResource[] children) {
		this.children = children;
	}
	/*
	 * @see ICVSRemoteFolder#setTag(String)
	 */
	public void setTag(CVSTag tag) {
		this.folderInfo = new FolderSyncInfo(folderInfo.getRepository(), folderInfo.getRoot(), tag, folderInfo.getIsStatic());
	}

	/*
	 * @see ICVSRemoteFolder#getTag()
	 */
	public CVSTag getTag() {
		return folderInfo.getTag();
	}
	/*
	 * @see ICVSFolder#setFolderInfo(FolderSyncInfo)
	 */
	public void setFolderSyncInfo(FolderSyncInfo folderInfo) throws CVSException {
		this.folderInfo = folderInfo;
		// XXX temporary to see if this ever occurs
		throw new CVSException(Policy.bind("RemoteResource.invalidOperation"));//$NON-NLS-1$
	}
	
	/**
	 * Update the file revision for the given child such that the revision is the one in the given branch.
	 * Return true if the file exists and false otherwise
	 */
	protected boolean updateRevision(ICVSRemoteFile child, CVSTag tag, IProgressMonitor monitor) throws CVSException {
		
		ICVSRemoteResource[] oldChildren = children;
		try {
			children = new ICVSRemoteResource[] {child};
			
			// Create the listener for remote files and folders
			final boolean[] exists = new boolean[] {true};
			IUpdateMessageListener listener = new IUpdateMessageListener() {
				public void directoryInformation(IPath path, boolean newDirectory) {
				}
				public void directoryDoesNotExist(IPath path) {
					// If we get this, we can assume that the parent directory no longer exists
					exists[0] = false;
				}
				public void fileInformation(int type, String filename) {
					// The file was found and has a different revision
					try {
						((RemoteFile)getChild(filename)).setWorkspaceSyncState(type);
					} catch(CVSException e) {
						exists[0] = false;
					}
					exists[0] = true;
				}
				public void fileDoesNotExist(String filename) {
					exists[0] = false;
				}
			};
			
			// Build the local options
			List localOptions = new ArrayList();
			if (tag != null && tag.getType() != CVSTag.HEAD)
				localOptions.add(Update.makeTagOption(tag));
			
			// Retrieve the children and any file revision numbers in a single connection
			Session s = new Session(getRepository(), this, false);
			s.open(monitor);
			try {
				// Perform a "cvs -n update -d -r tagName fileName" with custom message and error handlers
				Command.UPDATE.execute(s,
					new GlobalOption[] { Command.DO_NOT_CHANGE },
					(LocalOption[]) localOptions.toArray(new LocalOption[localOptions.size()]), 
					new String[] { child.getName() },
					new UpdateListener(listener),
					monitor);
	
				if (!exists[0]) return false;		
				updateFileRevisions(s, new String[] {child.getName()}, monitor);
				return true;
				
			} finally {
				s.close();
			}
		} finally {
			children = oldChildren;
		}
	}
	
	/*
	 * @see ICVSFolder#run(ICVSRunnable, IProgressMonitor)
	 */
	public void run(ICVSRunnable job, IProgressMonitor monitor) throws CVSException {
		job.run(monitor);
	}
	
	/*
	 * @see ICVSFolder#tag(CVSTag, LocalOption[], IProgressMonitor)
	 */
	public IStatus tag(CVSTag tag, LocalOption[] localOptions, IProgressMonitor monitor) throws CVSException {
	 	monitor.beginTask(null, 100);
		Session s = new Session(getRepository(), this, false);
		s.open(Policy.subMonitorFor(monitor, 10));
		try {
			return Command.RTAG.execute(s,
				Command.NO_GLOBAL_OPTIONS,
				localOptions,
				folderInfo.getTag(),
				tag,
				new String[] { folderInfo.getRepository() },
				Policy.subMonitorFor(monitor, 90));
			
		} finally {
			s.close();
			monitor.done();
		}
	 }
}