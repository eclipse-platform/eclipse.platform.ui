package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTree;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.response.IResponseHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.IStatusListener;
import org.eclipse.team.internal.ccvs.core.response.custom.IUpdateMessageListener;
import org.eclipse.team.internal.ccvs.core.response.custom.StatusMessageHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.UpdateErrorHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.UpdateMessageHandler;

/*
 * This class is responsible for building a remote tree that shows the repository
 * state of a locally loaded folder tree.
 * 
 * It is used as follows
 * 
 * 		RemoteFolderTreeBuilder.buildRemoteTree(CVSRepositoryLocation, IManagedFolder, String, IProgressMonitor);
 * 
 * The provider IManagedFolder can be a local resource or a RemoteFolderTree that
 * that was previously built.
 */
public class RemoteFolderTreeBuilder {

	private Map fileDeltas;
	private List changedFiles;
	
	private List errors;
	
	private ICVSFolder root;
	private RemoteFolderTree remoteRoot;
	private CVSRepositoryLocation repository;
	
	private CVSTag tag;
	
	private String[] updateLocalOptions;
	
	private static String UNKNOWN = "";
	private static String DELETED = "DELETED";
	private static String ADDED = "ADDED";
	private static String FOLDER = "FOLDER";
	
	private static Map EMPTY_MAP = new HashMap();
	
	private RemoteFolderTreeBuilder(CVSRepositoryLocation repository, ICVSFolder root, CVSTag tag) {
		this.repository = repository;
		this.root = root;
		this.tag = tag;
		this.errors = new ArrayList();
		this.fileDeltas = new HashMap();
		this.changedFiles = new ArrayList();
		
		// Build the local options
		List localOptions = new ArrayList();
		localOptions.add("-d");
		if ((tag != null) && (tag.getType() != tag.HEAD)) {
			localOptions.add(Client.TAG_OPTION);
			localOptions.add(tag.getName());
		}
		updateLocalOptions = (String[])localOptions.toArray(new String[localOptions.size()]);
	}
	
	public static RemoteFolderTree buildRemoteTree(CVSRepositoryLocation repository, IContainer root, CVSTag tag, IProgressMonitor monitor) throws CVSException {
		return buildRemoteTree(repository, Client.getManagedFolder(root.getLocation().toFile()), tag, monitor);
	}
	
	public static RemoteFolderTree buildRemoteTree(CVSRepositoryLocation repository, ICVSFolder root, CVSTag tag, IProgressMonitor monitor) throws CVSException {
		RemoteFolderTreeBuilder builder = new RemoteFolderTreeBuilder(repository, root, tag);
 		return builder.buildTree(monitor);
	}
	 
	private RemoteFolderTree buildTree(IProgressMonitor monitor) throws CVSException {
		Connection connection = repository.openConnection();
		try {
			fetchDelta(connection, monitor);
			remoteRoot = new RemoteFolderTree(null, repository, new Path(root.getFolderSyncInfo().getRepository()), tag);
			buildRemoteTree(connection, root, remoteRoot, Path.EMPTY, monitor);
			if (!changedFiles.isEmpty())
				fetchFileRevisions(connection, remoteRoot, (String[])changedFiles.toArray(new String[changedFiles.size()]), monitor);
			return remoteRoot;
		} catch (CVSException e) {
			if (!errors.isEmpty()) {
				PrintStream out = getPrintStream();
				for (int i=0;i<errors.size();i++)
					out.println(errors.get(i));
			}
			throw e;
		} finally {
			connection.close();
		}
	}
	
	/*
	 * Build the remote tree from the local tree and the recorded deltas.
	 * 
	 * The localPath is used to retrieve deltas from the recorded deltas
	 * 
	 */
	private void buildRemoteTree(Connection connection, ICVSFolder local, RemoteFolderTree remote, IPath localPath, IProgressMonitor monitor) throws CVSException {
		
		// Create a map to contain the created children
		Map children = new HashMap();
		
		// If there's no corresponding local resource then we need to fetch its contents in order to populate the deltas
		if (local == null) {
			fetchNewDirectory(connection, remote, localPath, monitor);
		}
		
		// Fetch the delta's for the folder
		Map deltas = deltas = (Map)fileDeltas.get(localPath);
		if (deltas == null)
			deltas = EMPTY_MAP;
		
		// If there is a local, use the local children to start buidling the remote children
		if (local != null) {
			// Build the child folders corresponding to local folders
			ICVSFolder[] folders = local.getFolders();
			for (int i=0;i<folders.length;i++) {
				if (folders[i].isCVSFolder() && (deltas.get(folders[i].getName()) != DELETED))
					children.put(folders[i].getName(), new RemoteFolderTree(remote, repository, new Path(folders[i].getFolderSyncInfo().getRepository()), tag));
			}
			// Build the child files corresponding to local files
			ICVSFile[] files = local.getFiles();
			for (int i=0;i<files.length;i++) {
				ICVSFile file = files[i];
				if (deltas.get(file.getName()) != DELETED) {
					ResourceSyncInfo info = file.getSyncInfo();
					// if there is no sync info then there isn't a remote file for this local file on the
					// server.
					if ((info!=null) && (!info.isAdded())) {
						children.put(file.getName(), new RemoteFile(remote, info));
					}
				}
			}
		}
		
		// Build the children for new or out-of-date resources from the deltas
		Iterator i = deltas.keySet().iterator();
		while (i.hasNext()) {
			String name = (String)i.next();
			String revision = (String)deltas.get(name);
			if (revision == FOLDER) {
				// XXX should getRemotePath() return an IPath instead of a String?
				children.put(name, new RemoteFolderTree(remote, repository, new Path(remote.getRemotePath()).append(name), tag));
			} else if (revision == ADDED) {
				children.put(name, new RemoteFile(remote, name, tag));
			} else if (revision == UNKNOWN) {
				// This should have been created from the local resources.
				// If it wasn't, we'll add it!
				if (!children.containsKey(name))
					children.put(name, new RemoteFile(remote, name, tag));
			} else if (revision == DELETED) {
				// This should have been deleted while creating from the local resources.
				// If it wasn't, delete it now.
				if (children.containsKey(name))
					children.remove(name);
			} else {
				// We should never get here
			}
		}

		// Add the children to the remote folder tree
		remote.setChildren((ICVSRemoteResource[])children.values().toArray(new ICVSRemoteResource[children.size()]));
		
		// We have to delay building the child folders to support the proper fetching of new directories
		// due to the fact that the same CVS home directory (i.e. the same root directory) must
		// be used for all requests sent over the same connection
		Iterator childIterator = children.entrySet().iterator();
		List emptyChildren = new ArrayList();
		while (childIterator.hasNext()) {
			Map.Entry entry = (Map.Entry)childIterator.next();
			if (((RemoteResource)entry.getValue()).isFolder()) {
				RemoteFolderTree remoteFolder = (RemoteFolderTree)entry.getValue();
				String name = (String)entry.getKey();
				ICVSFolder localFolder;
				if (deltas.get(name) == FOLDER)
					localFolder = null;
				else
					localFolder = local.getFolder(name);
				buildRemoteTree(connection, localFolder, remoteFolder, localPath.append(name), monitor);
				// Record any children that are empty
				if (pruneEmptyDirectories() && remoteFolder.getChildren().length == 0) {
					emptyChildren.add(remoteFolder);
				}
			}
		}
		
		// Prune any empty child folders
		if (pruneEmptyDirectories() && !emptyChildren.isEmpty()) {
			List newChildren = new ArrayList();
			newChildren.addAll(Arrays.asList(remote.getChildren()));
			newChildren.removeAll(emptyChildren);
			remote.setChildren((ICVSRemoteResource[])newChildren.toArray(new ICVSRemoteResource[newChildren.size()]));

		}
	}
	
	/*
	 * This method fetches the delta between the local state and the remote state of the resource tree
	 * and records the deltas in the fileDeltas instance variable
	 * 
	 * Returns the list of changed files
	 */
	private List fetchDelta(Connection connection, IProgressMonitor monitor) throws CVSException {
		
		// Create an listener that will accumulate new and removed files and folders
		final List newChildDirectories = new ArrayList();
		IUpdateMessageListener listener = new IUpdateMessageListener() {
			public void directoryInformation(IPath path, boolean newDirectory) {
				if (newDirectory) {
					// Record new directory with parent so it can be retrieved when building the parent
					recordDelta(path, FOLDER);
					// Record new directory to be used as a parameter to fetch its contents
					newChildDirectories.add(path.toString());
				}
			}
			public void directoryDoesNotExist(IPath path) {
				// Record removed directory with parent so it can be removed when building the parent
				recordDelta(path, DELETED);
			}
			public void fileInformation(char type, String filename) {
				// Cases that do not require action are:
				// 	case 'A' :  = A locally added file that does not exists remotely
				//	case '?' :  = A local file that has not been added and does not exists remotely
				//  case 'M' :  = A locally modified file that has not been modified remotely
				switch(type) {
					case 'R' : // We have a locally removed file that still exists remotely
					case 'U' : // We have an remote change to an unmodified local file
					case 'C' : // We have an remote change to a modified local file
								changedFiles.add(filename);
								recordDelta(new Path(filename), UNKNOWN);
								break;
				}	
			}
			// NOTE: We don't have the proper handling for deleted files.
			// Support needs to be added to the error handler
			public void fileDoesNotExist(String filename) {
				recordDelta(new Path(filename), DELETED);
			}
		};
		
		// Perform a "cvs -n update -d [-r tag] ." in order to get the
		// messages from the server that will indicate what has changed on the 
		// server.
		Client.execute(
			Client.UPDATE,
			new String[] {"-n"}, 
			updateLocalOptions,
			new String[]{"."}, 
			root,
			monitor,
			getPrintStream(),
			connection,
			new IResponseHandler[]{new UpdateMessageHandler(listener), new UpdateErrorHandler(listener, errors)},
			true
			);
					
		return changedFiles;
	}
	
	private void fetchNewDirectory(Connection connection, RemoteFolderTree newFolder, IPath localPath, IProgressMonitor monitor) throws CVSException {
		
		// Create an listener that will accumulate new files and folders
		IUpdateMessageListener listener = new IUpdateMessageListener() {
			public void directoryInformation(IPath path, boolean newDirectory) {
				if (newDirectory) {
					// Record new directory with parent so it can be retrieved when building the parent
					// NOTE: Check path prefix
					recordDelta(path, FOLDER);
				}
			}
			public void directoryDoesNotExist(IPath path) {
			}
			public void fileInformation(char type, String filename) {
				// NOTE: Check path prefix
				changedFiles.add(filename);
				recordDelta(new Path(filename), ADDED);
			}
			public void fileDoesNotExist(String filename) {
			}
		};

		// NOTE: Should use the path relative to the remoteRoot
		IPath path = new Path(newFolder.getRemotePath());
		Client.execute(
			Client.UPDATE,
			new String[] {"-n"}, 
			updateLocalOptions,
			new String[] {localPath.toString()}, 
			remoteRoot,
			monitor,
			getPrintStream(),
			connection,
			new IResponseHandler[]{new UpdateMessageHandler(listener), new UpdateErrorHandler(listener, errors)},
			false
			);
	}
	
	// Get the file revisions for the given filenames
	private void fetchFileRevisions(Connection connection, final RemoteFolder root, String[] fileNames, IProgressMonitor monitor) throws CVSException {
		
		// Create a listener for receiving the revision info
		final int[] count = new int[] {0};
		final Map revisions = new HashMap();
		IStatusListener listener = new IStatusListener() {
			public void fileStatus(IPath path, String remoteRevision) {
				try {
					updateRevision(root, path, remoteRevision);
					count[0]++;
				} catch (CVSException e) {
					// The count will be off which will trigger another exception
					CVSProviderPlugin.log(e);
				}
			}
		};
			
		// Perform a "cvs status..." with a custom message handler
		Client.execute(
			Client.STATUS,
			Client.EMPTY_ARGS_LIST, 
			Client.EMPTY_ARGS_LIST,
			fileNames,
			root,
			monitor,
			getPrintStream(),
			connection,
			new IResponseHandler[] {new StatusMessageHandler(listener)},
			false);
		
		// XXX we can't make this check because it may be valid to call this method
		// without any file names (e.g. fileNames array empty) which would run the
		// status on all files.
		//if (count[0] != fileNames.length)
		//	throw new CVSException(Policy.bind("RemoteFolder.errorFetchingRevisions"));
	}


	private PrintStream getPrintStream() {
		return CVSProviderPlugin.getProvider().getPrintStream();
	}
	
	private boolean pruneEmptyDirectories() {
		return true;
	}
	/*
	 * Record the deltas in a double map where the outer key is the parent directory
	 * and the inner key is the file name. The value is the revision of the file or
	 * DELETED (file or folder). New folders have a revision of FOLDER.
	 * 
	 * A revison of UNKNOWN indicates that the revision has not been fetched
	 * from the repository yet.
	 */
	private void recordDelta(IPath path, String revision) {
		IPath parent = path.removeLastSegments(1);
		Map deltas = (Map)fileDeltas.get(parent);
		if (deltas == null) {
			deltas = new HashMap();
			fileDeltas.put(parent, deltas);
		}
		deltas.put(path.lastSegment(), revision);
	}
	
	private void updateRevision(RemoteFolder root, IPath path, String revision) throws CVSException {
		((RemoteFile)root.getFile(path.toString())).setRevision(revision);
	}
}

