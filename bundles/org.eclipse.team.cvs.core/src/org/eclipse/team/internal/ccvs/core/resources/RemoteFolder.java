package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.response.IResponseHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.IStatusListener;
import org.eclipse.team.internal.ccvs.core.response.custom.IUpdateMessageListener;
import org.eclipse.team.internal.ccvs.core.response.custom.StatusErrorHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.StatusMessageHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.UpdateErrorHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.UpdateMessageHandler;

/**
 * This class provides the implementation of ICVSRemoteFolder
 */
public class RemoteFolder extends RemoteResource implements ICVSRemoteFolder, ICVSFolder {

	private ICVSRemoteResource[] children;
	private CVSRepositoryLocation repository;
	private IPath repositoryRelativePath;
	private CVSTag tag;
	
	/**
	 * Constructor for RemoteFolder.
	 */
	public RemoteFolder(RemoteFolder parent, ICVSRepositoryLocation repository, IPath repositoryRelativePath, CVSTag tag) {
		String name = repositoryRelativePath.lastSegment() == null ? "" : repositoryRelativePath.lastSegment();
		this.info = new ResourceSyncInfo(name);
		this.parent = parent;
		this.tag = tag;
		this.repository = (CVSRepositoryLocation)repository;
		this.repositoryRelativePath = repositoryRelativePath;		
	}

	// Get the file revisions for the given filenames
	protected void updateFileRevisions(Connection connection, String[] fileNames, IProgressMonitor monitor) throws CVSException {
		
		final int[] count = new int[] {0};
		
		// Create a listener for receiving the revision info
		final Map revisions = new HashMap();
		IStatusListener listener = new IStatusListener() {
			public void fileStatus(IPath path, String remoteRevision) {
				if (remoteRevision == IStatusListener.FOLDER_RIVISION)
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
			
		// Perform a "cvs status..." with a custom message handler
		final List errors = new ArrayList();
		try {
		Client.execute(
			Client.STATUS,
			Client.EMPTY_ARGS_LIST, 
				Client.EMPTY_ARGS_LIST,
			fileNames,
			this,
			monitor,
			getPrintStream(),
			connection,
				new IResponseHandler[] {new StatusMessageHandler(listener),new StatusErrorHandler(listener, errors)},
			false);
		} catch (CVSException e) {
			if (!errors.isEmpty()) {
				PrintStream out = getPrintStream();
				for (int i=0;i<errors.size();i++)
					out.println(errors.get(i));
			}
			throw e;
		}
		
		if (count[0] != fileNames.length)
			throw new CVSException(Policy.bind("RemoteFolder.errorFetchingRevisions"));
	}
	
	/**
	 * @see IManagedResource#accept(IManagedVisitor)
	 */
	public void accept(ICVSResourceVisitor visitor) throws CVSException {
		visitor.visitFolder(this);
	}

	/**
	 * Check whether the given resource is a child of the receiver remotely
	 */
	protected boolean exists(ICVSRemoteResource child, IProgressMonitor monitor) throws CVSException {
		return exists(child, getTag(), monitor);
	}
	
	/**
	 * Check whether the child exists for the given tag. This additional method is required because
	 * CVS will signal an error if a folder only contains subfolders when a tag is used. If we get this
	 * error and we're looking for a folder, we need to reissue the command without a tag.
	 */
	protected boolean exists(ICVSRemoteResource child, CVSTag tag, IProgressMonitor monitor) throws CVSException {
		
		final IProgressMonitor progress = Policy.monitorFor(monitor);
		
		// Create the listener for remote files and folders
		final List errors = new ArrayList();
		final boolean[] exists = new boolean[] {false};
		final boolean expectError[] = new boolean[] {false};
		IUpdateMessageListener listener = new IUpdateMessageListener() {
			public void directoryInformation(IPath path, boolean newDirectory) {
				exists[0] = true;
			}
			public void directoryDoesNotExist(IPath path) {
			}
			public void fileInformation(char type, String filename) {
				exists[0] = true;
			}
			public void fileDoesNotExist(String filename) {
			}
			public void expectError() {
				expectError[0] = true;
			}
		};
		
		// Build the local options
		List localOptions = new ArrayList();
		localOptions.add("-d");
		if ((tag != null) && (tag.getType() != CVSTag.HEAD)) {
			localOptions.add(Client.TAG_OPTION);
			localOptions.add(tag.getName());
		}
		
		// Retrieve the children and any file revision numbers in a single connection
		try {
			// Perform a "cvs -n update -d -r tagName folderName" with custom message and error handlers
			Client.execute(
				Client.UPDATE,
				new String[]{Client.NOCHANGE_OPTION}, 
				(String[])localOptions.toArray(new String[localOptions.size()]), 
				new String[]{child.getName()}, 
				this,
				monitor,
				getPrintStream(),
				((CVSRepositoryLocation)getRepository()),
				new IResponseHandler[]{new UpdateMessageHandler(listener), new UpdateErrorHandler(listener, errors)});

			return exists[0];
			
		} catch (CVSServerException e) {
			if ( ! isNoTagException(errors) || ! child.isContainer())
				if (!expectError[0])
					throw e;
				// we now know that this is an exception caused by a cvs bug.
				// if the folder has no files in it (just subfolders) cvs does not respond with the subfolders...
				// workaround: retry the request with no tag to get the directory names (if any)
			Policy.checkCanceled(progress);
			return exists(child, null, progress);
		} catch (CVSException e) {
			if (!errors.isEmpty()) {
				PrintStream out = getPrintStream();
				for (int i=0;i<errors.size();i++)
					out.println(errors.get(i));
			}
			throw e;
		}
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
		final List errors = new ArrayList();
		final List newRemoteDirectories = new ArrayList();
		final List newRemoteFiles = new ArrayList();
		final boolean expectError[] = new boolean[] {false};
		IUpdateMessageListener listener = new IUpdateMessageListener() {
			public void directoryInformation(IPath path, boolean newDirectory) {
				if (newDirectory && path.segmentCount() == 1) {
					newRemoteDirectories.add(path.lastSegment());
					progress.subTask(path.lastSegment().toString());
					progress.worked(1);
				}
			}
			public void directoryDoesNotExist(IPath path) {
			}
			public void fileInformation(char type, String filename) {
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
			public void expectError() {
				expectError[0] = true;
			}
		};
		
		// Build the local options
		List localOptions = new ArrayList();
		localOptions.add("-d");
		if ((tag != null) && (tag.getType() != CVSTag.HEAD)) {
			localOptions.add(Client.TAG_OPTION);
			localOptions.add(tag.getName());
		}
		
		// Retrieve the children and any file revision numbers in a single connection
		Connection c = ((CVSRepositoryLocation)getRepository()).openConnection();
		try {
			// Perform a "cvs -n update -d -r tagName folderName" with custom message and error handlers
			Client.execute(
				Client.UPDATE,
				new String[]{Client.NOCHANGE_OPTION}, 
				(String[])localOptions.toArray(new String[localOptions.size()]), 
				new String[]{"."}, 
				this,
				monitor,
				getPrintStream(),
				c,
				new IResponseHandler[]{new UpdateMessageHandler(listener), new UpdateErrorHandler(listener, errors)},
				true);

			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}

			// Convert the file and folder names to IManagedResources
			List result = new ArrayList();
			for (int i=0;i<newRemoteFiles.size();i++) {
				result.add(new RemoteFile(this, (String)newRemoteFiles.get(i), tag));
			}
			for (int i=0;i<newRemoteDirectories.size();i++)
				result.add(new RemoteFolder(this, getRepository(), repositoryRelativePath.append((String)newRemoteDirectories.get(i)), tag));
			children = (ICVSRemoteResource[])result.toArray(new ICVSRemoteResource[0]);

			// Get the revision numbers for the files
			if (newRemoteFiles.size() > 0) {
				updateFileRevisions(c, (String[])newRemoteFiles.toArray(new String[newRemoteFiles.size()]), monitor);
			}
			
		} catch (CVSServerException e) {
			if ( ! isNoTagException(errors) && ! expectError[0])
				throw e;
				// we now know that this is an exception caused by a cvs bug.
				// if the folder has no files in it (just subfolders) cvs does not respond with the subfolders...
				// workaround: retry the request with no tag to get the directory names (if any)
			Policy.checkCanceled(progress);
			children = getMembers(null, progress);
		} catch (CVSException e) {
			if (!errors.isEmpty()) {
				PrintStream out = getPrintStream();
				for (int i=0;i<errors.size();i++)
					out.println(errors.get(i));
			}
			throw e;
		} finally {
			c.close();
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
		if (name.equals(Client.CURRENT_LOCAL_FOLDER) || name.equals(Client.CURRENT_LOCAL_FOLDER + Client.SERVER_SEPARATOR))
			return this;
		ICVSResource child = getChild(name);
		if (child.isFolder())
			return (ICVSFolder)child;
		throw new CVSException(Policy.bind("RemoteFolder.invalidChild", new Object[] {name}));
	}

	/**
	 * @see ICVSFolder#getFile(String)
	 */
	public ICVSFile getFile(String name) throws CVSException {
		ICVSResource child = getChild(name);
		if (!child.isFolder())
			return (ICVSFile)child;
		throw new CVSException(Policy.bind("RemoteFolder.invalidChild", new Object[] {name}));

	}

	public String getRemotePath() {
		return repositoryRelativePath.toString();
	}
	
	/**
	 * @see ICVSResource#getRelativePath(ICVSFolder)
	 */
	public String getRelativePath(ICVSFolder ancestor) throws CVSException {
		if (ancestor == this)
			return ".";
		// NOTE: This is a quick and dirty way.
		return this.getRemotePath().substring(((RemoteFolder)ancestor).getRemotePath().length() + 1);
		// throw new CVSException(Policy.bind("RemoteFolder.invalidOperation"));
	}
	
	public ICVSRepositoryLocation getRepository() {
		return repository;
	}
	
	/**
	 * @see ICVSResource#isFolder()
	 */
	public boolean isFolder() {
		return true;
	}
	
	/**
	 * Return true if the exception from the cvs server is the no tag error, and false
	 * otherwise.
	 */
	protected boolean isNoTagException(List errors) {
		if (errors.size() != 1)
			return false;
		if (((IStatus)errors.get(0)).getMessage().startsWith("cvs [server aborted]: no such tag"))
			return true;
		return false;
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
		if (path.equals(Client.CURRENT_LOCAL_FOLDER))
			return this;
		ICVSRemoteResource[] children = getChildren();
		if (children == null) 
			throw new CVSException(Policy.bind("RemoteFolder.invalidChild", new Object[] {getName()}));
		if (path.indexOf(Client.SERVER_SEPARATOR) == -1) {
			for (int i=0;i<children.length;i++) {
				if (children[i].getName().equals(path))
					return (ICVSResource)children[i];
			}
		} else {
			IPath p = new Path(path);
			return ((RemoteFolder)getChild(p.segment(0))).getChild(p.removeFirstSegments(1).toString());
		}
		throw new CVSException(Policy.bind("RemoteFolder.invalidChild", new Object[] {getName()}));
	}

	/**
	 * @see ICVSFolder#mkdir()
	 */
	public void mkdir() throws CVSException {
		throw new CVSException(Policy.bind("RemoteResource.invalidOperation"));
	}

	/**
	 * @see ICVSFolder#flush(boolean)
	 */
	public void flush(boolean deep) {
	}

	/**
	 * @see ICVSFolder#getFolderInfo()
	 */
	public FolderSyncInfo getFolderSyncInfo() throws CVSException {
		return new FolderSyncInfo(getRemotePath(), getRepository().getLocation(), getTag(), false);
	}

	/**
	 * @see ICVSResource#getRemoteLocation(ICVSFolder)
	 */
	public String getRemoteLocation(ICVSFolder stopSearching) throws CVSException {
		return getRepository().getRootDirectory() + Client.SERVER_SEPARATOR + getRemotePath();
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
		throw new CVSException(Policy.bind("RemoteResource.invalidOperation"));
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
		this.tag = tag;
	}

	/*
	 * @see ICVSRemoteFolder#getTag()
	 */
	public CVSTag getTag() {
		return tag;
	}
	/*
	 * @see ICVSFolder#setFolderInfo(FolderSyncInfo)
	 */
	public void setFolderSyncInfo(FolderSyncInfo folderInfo) throws CVSException {
	}
	
	/**
	 * Update the file revision for the given child such that the revision is the one in the given branch.
	 * Return true if the file exists and false otherwise
	 */
	protected boolean updateRevision(ICVSRemoteFile child, CVSTag tag, IProgressMonitor monitor) throws CVSException {
		
		ICVSRemoteResource[] oldChildren = children;
		try {
			children = new ICVSRemoteResource[] {child};
			
			final IProgressMonitor progress = Policy.monitorFor(monitor);
			
			// Create the listener for remote files and folders
			final List errors = new ArrayList();
			final boolean[] exists = new boolean[] {true};
			final boolean expectError[] = new boolean[] {false};
			IUpdateMessageListener listener = new IUpdateMessageListener() {
				public void directoryInformation(IPath path, boolean newDirectory) {
				}
				public void directoryDoesNotExist(IPath path) {
					// If we get this, we can assume that the parent directory no longer exists
					exists[0] = false;
				}
				public void fileInformation(char type, String filename) {
					// The file was found and has a different revision
					exists[0] = true;
				}
				public void fileDoesNotExist(String filename) {
					exists[0] = false;
				}
				public void expectError() {
					expectError[0] = true;
				}
			};
			
			// Build the local options
			List localOptions = new ArrayList();
			if ((tag != null) && (tag.getType() != CVSTag.HEAD)) {
				localOptions.add(Client.TAG_OPTION);
				localOptions.add(tag.getName());
			}
			
			// Retrieve the children and any file revision numbers in a single connection
			Connection c = ((CVSRepositoryLocation)getRepository()).openConnection();
			try {
				// Perform a "cvs -n update -d -r tagName fileName" with custom message and error handlers
				try {
					Client.execute(
						Client.UPDATE,
						new String[]{Client.NOCHANGE_OPTION}, 
						(String[])localOptions.toArray(new String[localOptions.size()]), 
						new String[]{child.getName()}, 
						this,
						monitor,
						getPrintStream(),
						c,
						new IResponseHandler[]{new UpdateMessageHandler(listener), new UpdateErrorHandler(listener, errors)},
						true);
				} catch (CVSServerException e) {
					if (!expectError[0]) {
						throw e;
					}
				}
	
				if (!exists[0])
					return false;
					
				updateFileRevisions(c, new String[] {child.getName()}, monitor);
				return true;
				
			} catch (CVSException e) {
				if (!errors.isEmpty()) {
					PrintStream out = getPrintStream();
					for (int i=0;i<errors.size();i++)
						out.println(errors.get(i));
				}
				throw e;
			} finally {
				c.close();
			}
		} finally {
			children = oldChildren;
		}
	}
	/*
	 * @see ICVSRemoteFolder#getRelativePath()
	 */
	public String getRelativePath() {
		return getRemotePath();
	}
}