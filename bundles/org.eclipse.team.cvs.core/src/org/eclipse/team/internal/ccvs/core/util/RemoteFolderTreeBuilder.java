package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTree;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.response.IResponseHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.IStatusListener;
import org.eclipse.team.internal.ccvs.core.response.custom.IUpdateMessageListener;
import org.eclipse.team.internal.ccvs.core.response.custom.StatusMessageHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.UpdateErrorHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.UpdateMessageHandler;
import org.omg.CORBA.UNKNOWN;

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
	
	private IManagedFolder root;
	private RemoteFolderTree remoteRoot;
	private CVSRepositoryLocation repository;
	
	private String tag;
	
	private String[] updateLocalOptions;
	
	private static String UNKNOWN = "";
	private static String DELETED = "DELETED";
	private static String ADDED = "ADDED";
	private static String FOLDER = "FOLDER";
	
	private static Map EMPTY_MAP = new HashMap();
	
	private RemoteFolderTreeBuilder(CVSRepositoryLocation repository, IManagedFolder root, String tag) {
		this.repository = repository;
		this.root = root;
		this.tag = tag;
		this.errors = new ArrayList();
		this.fileDeltas = new HashMap();
		this.changedFiles = new ArrayList();
		
		// Build the local options
		List localOptions = new ArrayList();
		localOptions.add("-d");
		if ((tag != null) && (!tag.equals("HEAD"))) {
			localOptions.add(Client.TAG_OPTION);
			localOptions.add(tag);
		}
		updateLocalOptions = (String[])localOptions.toArray(new String[localOptions.size()]);
	}
	
	public static RemoteFolderTree buildRemoteTree(CVSRepositoryLocation repository, IContainer root, String tag, IProgressMonitor monitor) throws CVSException {
		return buildRemoteTree(repository, Client.getManagedFolder(root.getLocation().toFile()), tag, monitor);
	}
	
	public static RemoteFolderTree buildRemoteTree(CVSRepositoryLocation repository, IManagedFolder root, String tag, IProgressMonitor monitor) throws CVSException {
		RemoteFolderTreeBuilder builder = new RemoteFolderTreeBuilder(repository, root, tag);
 		return builder.buildTree(monitor);
	}
	 
	private RemoteFolderTree buildTree(IProgressMonitor monitor) throws CVSException {
		Connection connection = repository.openConnection();
		try {
			fetchDelta(connection, monitor);
			remoteRoot = new RemoteFolderTree(repository, new Path(root.getFolderInfo().getRepository()), tag);
			buildRemoteTree(connection, root, remoteRoot, Path.EMPTY, monitor);
			fetchFileRevisions(connection, remoteRoot, (String[])changedFiles.toArray(new String[changedFiles.size()]), monitor);
			return remoteRoot;
		} catch (CVSException e) {
			// throw CVSTeamProvider.wrapException(e, errors);
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
	 * If the remote is null, it is created from the local.
	 * If the local is null, the remote is used.
	 * 
	 * The localPath is used to retrieve deltas from the recorded deltas
	 * 
	 */
	private RemoteFolderTree buildRemoteTree(Connection connection, IManagedFolder local, RemoteFolderTree remote, IPath localPath, IProgressMonitor monitor) throws CVSException {
		
		// Pre-create the resulting tree so it can be used as the parent in RemoteFile creation
		RemoteFolderTree tree;
		if (remote == null)
			tree = new RemoteFolderTree(repository, new Path(local.getFolderInfo().getRepository()), tag);
		else
			tree = remote;
		
		// Create a map to contain the created children
		Map children = new HashMap();
		
		// Check if we have a local copy corresponding to the remote folder
		Map deltas;
		if (local == null) {
			// This is a new folder. We need to fetch its contents in order to populate the deltas
			fetchNewDirectory(connection, remote, localPath, monitor);
			deltas = (Map)fileDeltas.get(localPath);
			if (deltas == null)
				deltas = EMPTY_MAP;
		} else {
			// This is an existing folder. The deltas will already be populated
			deltas = (Map)fileDeltas.get(localPath);
			if (deltas == null)
				deltas = EMPTY_MAP;
			// Build the child folders corresponding to local folders
			IManagedFolder[] folders = local.getFolders();
			for (int i=0;i<folders.length;i++) {
				if (deltas.get(folders[i].getName()) != DELETED)
					children.put(folders[i].getName(), new RemoteFolderTree(repository, new Path(folders[i].getFolderInfo().getRepository()), tag));
			}
			// Build the child files corresponding to local files
			IManagedFile[] files = local.getFiles();
			for (int i=0;i<files.length;i++) {
				if (deltas.get(files[i].getName()) != DELETED)
					children.put(files[i].getName(), new RemoteFile(tree, files[i].getName(), files[i].getFileInfo().getVersion()));
			}
		}
		
		// Build the children for new resources from the deltas
		Iterator i = deltas.keySet().iterator();
		while (i.hasNext()) {
			String name = (String)i.next();
			String revision = (String)deltas.get(name);
			if (revision == FOLDER) {
				// NOTE: getRemotePath() should just return an IPath
				children.put(name, new RemoteFolderTree(repository, new Path(tree.getRemotePath()).append(name), tag));
			} else if (revision == ADDED) {
				children.put(name, new RemoteFile(tree, name, UNKNOWN));
			} else if (revision == UNKNOWN) {
				// This should have been created from the local resources.
				// If it wasn't, we'll add it!
				if (!children.containsKey(name))
					children.put(name, new RemoteFile(tree, name, UNKNOWN));
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
		tree.setChildren((ICVSRemoteResource[])children.values().toArray(new ICVSRemoteResource[children.size()]));
		
		// We have to delay building the children to support the proper fetching of new directories
		Iterator childIterator = children.entrySet().iterator();
		while (childIterator.hasNext()) {
			Map.Entry entry = (Map.Entry)childIterator.next();
			if (((RemoteResource)entry.getValue()).isFolder()) {
				RemoteFolderTree remoteFolder = (RemoteFolderTree)entry.getValue();
				String name = (String)entry.getKey();
				IManagedFolder localFolder;
				if (deltas.get(name) == FOLDER)
					localFolder = null;
				else
					localFolder = local.getFolder(name);
				buildRemoteTree(connection, localFolder, remoteFolder, localPath.append(name), monitor);
			}
		}
		return tree;
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
				switch(type) {
					case 'A' :
								changedFiles.add(filename);
								recordDelta(new Path(filename), ADDED);
								break;
					case 'U' : // fall through
					case 'C' : 
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
		};

		// NOTE: Should use the path relative to the remoteRoot
		IPath path = new Path(newFolder.getRemotePath());
		Client.execute(
			Client.UPDATE,
			new String[] {"-n"}, 
			updateLocalOptions,
			new String[] {path.removeFirstSegments(1).toString()}, 
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
		
		if (count[0] != fileNames.length)
			throw new CVSException(Policy.bind("RemoteFolder.errorFetchingRevisions"));
	}


	private PrintStream getPrintStream() {
		return CVSProviderPlugin.getProvider().getPrintStream();
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

