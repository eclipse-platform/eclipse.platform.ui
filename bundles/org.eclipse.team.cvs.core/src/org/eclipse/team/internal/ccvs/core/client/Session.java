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
package org.eclipse.team.internal.ccvs.core.client;
 
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.Util;
import org.eclipse.team.internal.core.streams.CRLFtoLFInputStream;
import org.eclipse.team.internal.core.streams.LFtoCRLFInputStream;
import org.eclipse.team.internal.core.streams.ProgressMonitorInputStream;
import org.eclipse.team.internal.core.streams.SizeConstrainedInputStream;

/**
 * Maintains CVS communication state for the lifetime of a connection
 * to a remote repository.  This class covers the initialization, use,
 * and eventual shutdown of a dialogue between a CVS client and a
 * remote server.  This dialogue may be monitored through the use of
 * a console.
 * 
 * Initially the Session is in a CLOSED state during which communication
 * with the server cannot take place.  Once OPENED, any number of commands
 * may be issued serially to the server, one at a time.  When finished, the
 * Session MUST be CLOSED once again to prevent eventual local and/or
 * remote resource exhaustion.  The session can either be discarded, or
 * re-opened for use with the same server though no state is persisted from
 * previous connections except for console attributes.
 * 
 * CVSExceptions are thrown only as a result of unrecoverable errors.  Once
 * this happens, commands must no longer be issued to the server.  If the
 * Session is in the OPEN state, it is still the responsibility of the
 * caller to CLOSE it before moving on.
 */
public class Session {
	public static final String CURRENT_LOCAL_FOLDER = "."; //$NON-NLS-1$
	public static final String CURRENT_REMOTE_FOLDER = ""; //$NON-NLS-1$
	public static final String SERVER_SEPARATOR = "/"; //$NON-NLS-1$

	// default file transfer buffer size (in bytes)
	private static final int TRANSFER_BUFFER_SIZE = 8192;
	// update progress bar in increments of this size (in bytes)
	//   no incremental progress shown for files smaller than this size
	private static final int TRANSFER_PROGRESS_INCREMENT = 32768;

	private static final boolean IS_CRLF_PLATFORM = Arrays.equals(
		System.getProperty("line.separator").getBytes(), new byte[] { '\r', '\n' }); //$NON-NLS-1$

	private static Map currentOpenSessions = null;
	private static final int MAX_SESSIONS_OPEN = 10;
	
	private CVSRepositoryLocation location;
	private ICVSFolder localRoot;
	private boolean outputToConsole;
	private Connection connection = null;
	private String validRequests = null;
	private Date modTime = null;
	private boolean noLocalChanges = false;
	private boolean createBackups = true;
	private int compressionLevel = 0;
	private List expansions;
	private Collection /* of ICVSFile */ textTransferOverrideSet = null;
	private boolean hasBeenConnected = false;
	private Map caseMappings;

	// The resource bundle key that provides the file sending message
	private String sendFileTitleKey;

	/**
	 * Creates a new CVS session, initially in the CLOSED state.
	 * By default, command output is directed to the console.
	 * 
	 * @param location the CVS repository location used for this session
	 * @param localRoot represents the current working directory of the client
	 */
	public Session(ICVSRepositoryLocation location, ICVSFolder localRoot) {
		this(location, localRoot, true);
	}
	
	/**
	 * Creates a new CVS session, initially in the CLOSED state.
	 * 
	 * @param location the CVS repository location used for this session
	 * @param localRoot represents the current working directory of the client
	 * @param outputToConsole if true, command output is directed to the console
	 */
	public Session(ICVSRepositoryLocation location, ICVSFolder localRoot, boolean outputToConsole) {
		this.location = (CVSRepositoryLocation) location;
		this.localRoot = localRoot;
		this.outputToConsole = outputToConsole;
	}
	
	/**
	 * Execute the given runnable in a context that has access to an open session
	 * 
	 * A session will be opened for the provided root. If the root is null, no session is opened.
	 * However, sessions will be open for nested calls to run and these sessions will not be closed
	 * until the outer most run finishes.
	 */
	public static void run(ICVSRepositoryLocation location, ICVSFolder root, boolean outputToConsole,
		ICVSRunnable runnable, IProgressMonitor monitor) throws CVSException {
		
		// Determine if we are nested or not
		boolean isOuterRun = false;
		if (currentOpenSessions == null) {
			// Initialize the variable to be a map
			currentOpenSessions = new HashMap();
			isOuterRun = true;
		}
		
		try {
			monitor = Policy.monitorFor(monitor);
			monitor.beginTask(null, 100);
			if (root == null) {
				// We don't have a root, so just run the runnable
				// (assuming that nested runs will create sessions)
				runnable.run(Policy.subMonitorFor(monitor, 90));
			} else {
				// Open a session on the root as far up the chain as possible
				ICVSFolder actualRoot = root;
				while (actualRoot.isManaged()) {
					actualRoot = actualRoot.getParent();
				}
				// Look for the root in the current open sessions
				Session session = (Session)currentOpenSessions.get(actualRoot);
				if (session == null) {
					// Make sure we don't exceed our maximum
					if (currentOpenSessions.size() == MAX_SESSIONS_OPEN) {
						// Pick one at random and close it
						Object key = currentOpenSessions.keySet().iterator().next();
						try {
							((Session)currentOpenSessions.get(key)).close();
						} catch (CVSException e) {
							CVSProviderPlugin.log(e.getStatus());
						} finally {
							currentOpenSessions.remove(key);
						}
					}
					// If it's not there, open a session for the given root and remember it
					session = new Session(location, actualRoot, outputToConsole);
					session.open(Policy.subMonitorFor(monitor, 10));
					currentOpenSessions.put(actualRoot, session);
				}
				
				try {
					root.run(runnable, Policy.subMonitorFor(monitor, 100));
				} catch (CVSException e) {
					// The run didn't succeed so close the session and forget it ever existed
					try {
						// The session may have been close by a nested run
						if (currentOpenSessions.get(actualRoot) != null) session.close();
					} finally {
						currentOpenSessions.remove(actualRoot);
						throw e;
					}
				}
			}
		} finally {
			monitor.done();
			if (isOuterRun) {
				// Close all open sessions
				try {
					Iterator iter = currentOpenSessions.keySet().iterator();
					while (iter.hasNext()) {
						Session session = (Session)currentOpenSessions.get(iter.next());
						try {
							session.close();
						} catch (CVSException e) {
							CVSProviderPlugin.log(e.getStatus());
						}
					}
				} finally {
					currentOpenSessions = null;
				}
			}
		}
	}
	
	/**
	 * Answer the currently open session
	 */
	protected static Session getOpenSession(ICVSResource resource) throws CVSException {
		ICVSFolder root;
		if (resource.isFolder()) {
			root = (ICVSFolder)resource;
		} else {
			root = resource.getParent();
		}
		while (root.isManaged()) {
			root = root.getParent();
		}
		// Look for the root in the current open sessions
		return (Session)currentOpenSessions.get(root);
	}
	
	/** 
	 * Register a case collision with the session.
	 * 
	 * For folders, the desired path is where the folder should be and the actual path
	 * is where is was put temporarily. If one of the folders involved is pruned, the
	 * other can be placed properly (see Session#handleCaseCollisions())
	 * 
	 * For files, the desired path is where the file should be and the actual path is 
	 * the emtpy path indicating that the resource was not loaded.
	 * 
	 * This makes sense because the files in a folder are always communicated before the folders
	 * so a file can only collide with anothe file which can never be pruned so there's no
	 * point in loading the file in a temporary place.
	 */
	protected void addCaseCollision(String desiredLocalPath, String actualLocalPath) {
		if (caseMappings == null) caseMappings = new HashMap();
		IPath desiredPath = new Path(desiredLocalPath);
		IPath actualPath = new Path(actualLocalPath);
		Assert.isTrue(actualPath.equals(Path.EMPTY) || (desiredPath.segmentCount() == actualPath.segmentCount()));
		caseMappings.put(desiredPath, actualPath);
	}
	/*
	 * Add a module expansion receivered from the server.
	 * This is only used by the ModuleExpansionsHandler
	 */
	protected void addModuleExpansion(String expansion) {
		expansions.add(expansion);
	}
	
	/*
	 * Add a module expansion receivered from the server.
	 * This is only used by the ExpandModules command
	 */
	protected void resetModuleExpansion() {
		if (expansions == null) 
			expansions = new ArrayList();
		else
			expansions.clear();
	}
	
	/**
	 * Opens, authenticates and initializes a connection to the server specified
	 * for the remote location.
	 *
	 * @param monitor the progress monitor
	 * @throws IllegalStateException if the Session is not in the CLOSED state
	 */
	public void open(IProgressMonitor monitor) throws CVSException {
		if (connection != null) throw new IllegalStateException();
		monitor = Policy.monitorFor(monitor);
		monitor.beginTask(null, 100);
		try {
			connection = location.openConnection(Policy.subMonitorFor(monitor, 50));
			hasBeenConnected = true;
			
			ResponseHandler mtHandler = Request.getResponseHandler("MT"); //$NON-NLS-1$
			// accept MT messages for all non-standard server
			boolean useMT = ! (location.getServerPlatform() == location.CVS_SERVER);
			try {
				// If we're connected to a CVSNT server or we don't know the platform, 
				// accept MT. Otherwise don't.
				// We only want to disable MT messages for this particular session
				// since there may be multiple sessions open.
				if ( ! useMT) {
					Request.removeResponseHandler("MT"); //$NON-NLS-1$
				}
				
				// tell the server the names of the responses we can handle
				connection.writeLine("Valid-responses " + Request.makeResponseList()); //$NON-NLS-1$
			} finally {
				// Re-register the MT handler since there may be more than one session open
				if ( ! useMT) {
					Request.registerResponseHandler(mtHandler);
				}
			}
	
			// ask for the set of valid requests
			Request.VALID_REQUESTS.execute(this, Policy.subMonitorFor(monitor, 40));
			
			// set the root directory on the server for this connection
			connection.writeLine("Root " + getRepositoryRoot()); //$NON-NLS-1$

			// enable compression
			compressionLevel = CVSProviderPlugin.getPlugin().getCompressionLevel();
			if (compressionLevel != 0 && isValidRequest("gzip-file-contents")) { //$NON-NLS-1$
				// Enable the use of CVS 1.8 per-file compression mechanism.
				// The newer Gzip-stream request seems to be problematic due to Java's
				// GZIPInputStream tendency to block on read() rather than to return a
				// partially filled buffer.  The latter option would be better since it
				// can make more effective use of the code dictionary, if it can be made
				// to work...
				connection.writeLine("gzip-file-contents " + Integer.toString(compressionLevel)); //$NON-NLS-1$
			} else {
				compressionLevel = 0;
			}
			
			// get the server platform if it is unknown
			if (location.getServerPlatform() == location.UNDETERMINED_PLATFORM) {
				Command.VERSION.execute(this, location, Policy.subMonitorFor(monitor, 10));
			}
		} catch (CVSException e) {
			// If there is a failure opening, make sure we're closed
			if (connection != null) {
				hasBeenConnected = false;
				try {
					close();
				} catch (CVSException ex) {
					CVSProviderPlugin.log(ex);
				}
			}
			throw e;
		} finally {
			monitor.done();
		}
	}		
	
	/**
	 * Closes a connection to the server.
	 *
	 * @throws IllegalStateException if the Session is not in the OPEN state
	 */
	public void close() throws CVSException {
		if (connection == null) {
			if (hasBeenConnected) {
				throw new IllegalStateException();
			} else {
				return;
			}
		}
		connection.close();
		connection = null;
		validRequests = null;
	}
	
	/**
	 * Determines if the server supports the specified request.
	 * 
	 * @param request the request string to verify
	 * @return true iff the request is supported
	 */
	public boolean isValidRequest(String request) {
		return (validRequests == null) ||
			(validRequests.indexOf(" " + request + " ") != -1); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public boolean isCVSNT() {
		if (location.getServerPlatform() == location.UNDETERMINED_PLATFORM) {
			return location.getRootDirectory().indexOf(':') == 1;
		} else {
			return location.getServerPlatform() == location.CVSNT_SERVER;
		}
	}

	/**
	 * Return a local path that can be used to uniquely identify a resource
	 * if the platform does not support case variant names and there is a name collision
	 */
	protected String getUniquePathForCaseSensitivePath(String localPath, boolean creatingFolder) {
		IPath path = new Path(localPath);
		IPath existingMapping = null;
		if (caseMappings != null) {
			// Look for an existing parent path that has already been mapped
			for (int i = 0; i < path.segmentCount(); i++) {
				IPath key = path.removeLastSegments(i);
				existingMapping = (IPath)caseMappings.get(key);
				if (existingMapping != null) break;
			}
		}
		if (existingMapping != null) {
			if (existingMapping.segmentCount() == path.segmentCount()) {
				return existingMapping.toString();
			}
			// Convert the path to the mapped path
			path = existingMapping.append(path.removeFirstSegments(existingMapping.segmentCount()));
		}
		if (creatingFolder) {
			// Change the name of the folder to a case insensitive one
			String folderName = path.lastSegment();
			// XXX We should ensure that each permutation of characters is unique
			folderName = getUniqueNameForCaseVariant(folderName);
			path = path.removeLastSegments(1).append(folderName);
		}
		return path.toString();
	}
	
	/*
	 * Return a name that is unique for a give case variant.
	 */
	private String getUniqueNameForCaseVariant(String name) {
		char[] buffer = new char[name.length() * 2];
		int position = 0;
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			buffer[position++] = c;
			if (Character.isLetter(c)) {
				if (Character.isUpperCase(c)) {
					buffer[position++] = '-';
				} else {
					buffer[position++] = '_';
				}
			}		
		}
		return new String(buffer, 0, position);
	}
	
	/**
	 * Returns the local root folder for this session.
	 * <p>
	 * Generally speaking, specifies the "current working directory" at
	 * the time of invocation of an equivalent CVS command-line client.
	 * </p>
	 * 
	 * @return the local root folder
	 */
	public ICVSFolder getLocalRoot() {
		return localRoot;
	}

	/**
	 * Return the list of module expansions communicated from the server.
	 * 
	 * The modules expansions are typically a directory path of length 1
	 * but can be of greater length on occasion. 
	 */
	public String[] getModuleExpansions() {
		if (expansions == null) return new String[0];
		return (String[]) expansions.toArray(new String[expansions.size()]);
	}
	
	/**
	 * Returns the repository root folder for this session.
	 * <p>
	 * Specifies the unqualified path to the CVS repository root folder
	 * on the server.
	 * </p>
	 * 
	 * @return the repository root folder
	 */
	public String getRepositoryRoot() {
		return location.getRootDirectory();
	}
	
	/**
	 * Returns an object representing the CVS repository location for this session.
	 * 
	 * @return the CVS repository location
	 */
	public ICVSRepositoryLocation getCVSRepositoryLocation() {
		return location;
	}

	private IContainer getIResourceFor(ICVSFolder cvsFolder) throws CoreException, CVSException {
		if (cvsFolder.isManaged()) {
			return getIResourceFor(cvsFolder.getParent()).getFolder(new Path(cvsFolder.getName()));
		} else {
			return ResourcesPlugin.getWorkspace().getRoot().getProject(cvsFolder.getName());
		}
	}
	
	protected void handleCaseCollisions() throws CVSException {
		// Handle any case variant mappings
		Map mappings = caseMappings;
		if (mappings == null || mappings.size() == 0) return;
		// We need to start at the longest paths and work to the shortest
		// in case there are nested case collisions
		List sortedCollisions = new ArrayList();
		sortedCollisions.addAll(mappings.keySet());
		Collections.sort(sortedCollisions, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				int length0 = ((IPath)arg0).segmentCount();
				int length1 = ((IPath)arg1).segmentCount();
				if (length0 == length1) {
					return arg0.toString().compareTo(arg1.toString());
				}
				return length0 > length1 ? -1 : 1;
			}
		});
		// For each mapping, we need to see if one of the culprits was pruned
		List unhandledMappings = new ArrayList();
		Iterator iterator = sortedCollisions.iterator();
		while (iterator.hasNext()) {
			IPath desiredPath = (IPath)iterator.next();
			IPath actualPath = (IPath)mappings.get(desiredPath);
			// Check for the empty path (i.e. unloaded file)
			if (actualPath.equals(Path.EMPTY)) {
				unhandledMappings.add(desiredPath);
				continue;
			}
			// Check if the actualPath still exists (it may have been pruned)
			ICVSFolder actualFolder = getLocalRoot().getFolder(actualPath.toString());
			if ( ! actualFolder.exists()) continue;
			// Check if the desiredPath exists (we can only do this by trying to create it
			ICVSFolder desiredFolder = getLocalRoot().getFolder(desiredPath.toString());
			try {
				desiredFolder.mkdir();
				desiredFolder.delete();
			} catch (CVSException e) {
				// Must still exists. Delete the collision
				actualFolder.delete();
				actualFolder.unmanage(null);
				unhandledMappings.add(desiredPath);
				continue;
			}
			// The desired location is open (probably due to pruning)
			try {
				// We need to get the IResource for the actual and desired locations
				IResource actualResource = getIResourceFor(actualFolder);
				IResource desiredResource = actualResource.getParent().getFolder(new Path(desiredFolder.getName()));
				// Move the actual to the desired location
				actualResource.move(desiredResource.getFullPath(), false, null);
				// We need to also move the sync info. Since sync info is a session property
				// of the object, we can simpy reset the info for each moved resource
				desiredFolder.accept(new ICVSResourceVisitor() {
					public void visitFile(ICVSFile file) throws CVSException {
						file.setSyncInfo(file.getSyncInfo());
					}
					public void visitFolder(ICVSFolder folder) throws CVSException {
						folder.setFolderSyncInfo(folder.getFolderSyncInfo());
						folder.acceptChildren(this);
					}
				});
				// Unmanage the old location in order to remove the entry from the parent
				actualFolder.unmanage(null);
			} catch (CoreException e) {
				CVSProviderPlugin.log(e.getStatus());
				unhandledMappings.add(desiredPath);
			}
		}
		
		if (unhandledMappings.size() > 0) {
			MultiStatus status = new MultiStatus(CVSProviderPlugin.ID, CVSStatus.CASE_VARIANT_EXISTS, Policy.bind("PruneFolderVisitor.caseVariantsExist"), null);//$NON-NLS-1$
			Iterator iter = unhandledMappings.iterator();
			while (iter.hasNext()) {
				IPath desiredPath = (IPath) iter.next();
				IPath actualPath = (IPath)mappings.get(desiredPath);
				status.add(new CVSStatus(IStatus.ERROR, CVSStatus.CASE_VARIANT_EXISTS, 
					Policy.bind("PruneFolderVisitor.caseVariantExists", desiredPath.toString())));//$NON-NLS-1$		
			}
			if (status.getChildren().length == 1) {
				throw new CVSException(status.getChildren()[0]);
			} else {
				throw new CVSException(status);
			}
		}
	}
	
	/**
	 * Receives a line of text minus the newline from the server.
	 * 
	 * @return the line of text
	 */
	public String readLine() throws CVSException {
		return connection.readLine();
	}

	/**
	 * Sends a line of text followed by a newline to the server.
	 * 
	 * @param line the line of text
	 */
	public void writeLine(String line) throws CVSException {
		connection.writeLine(line);
	}

	/**
	 * Sends an argument to the server.
	 * <p>e.g. sendArgument("Hello\nWorld\n  Hello World") sends:
	 * <pre>
	 *   Argument Hello \n
	 *   Argumentx World \n
	 *   Argumentx Hello World \n
	 * </pre></p>
	 *
	 * @param arg the argument to send
	 */
	public void sendArgument(String arg) throws CVSException {
		connection.write("Argument "); //$NON-NLS-1$
		int oldPos = 0;
		for (;;) {
			int pos = arg.indexOf('\n', oldPos);
			if (pos == -1) break;
			connection.writeLine(arg.substring(oldPos, pos));
			connection.write("Argumentx "); //$NON-NLS-1$
			oldPos = pos + 1;
		}
		connection.writeLine(arg.substring(oldPos));
	}

	/**
	 * Sends a request to the server and flushes any output buffers.
	 * 
	 * @param requestId the string associated with the request to be executed
	 */
	public void sendRequest(String requestId) throws CVSException {
		connection.writeLine(requestId);
		connection.flush();
	}

	/**
	 * Sends an Is-modified request to the server without the file contents.
	 * <p>e.g. if a file called "local_file" was modified, sends:
	 * <pre>
	 *   Is-modified local_file \n
	 * </pre></p><p>
	 * This request is an optimized form of the Modified request and may not
	 * be supported by all servers.  Hence, if it is not supported, a Modified
	 * request is sent instead along with the file's contents.  According to
	 * the CVS protocol specification, this request is only safe for use with
	 * some forms of: admin, annotate, diff, editors, log, watch-add, watch-off,
	 * watch-on, watch-remove, and watchers.<br>
	 * It may be possible to use this for: add, export, remove and status.<br>
	 * Do not use with co, ci, history, init, import, release, rdiff, rtag, or update.
	 * </p><p>
	 * Note: The most recent Directory request must have specified the file's
	 *       parent folder.
	 * </p>
	 * 
	 * @param file the file that was modified
	 * @see #sendModified
	 */
	public void sendIsModified(ICVSFile file, boolean isBinary, IProgressMonitor monitor)
		throws CVSException {
		if (isValidRequest("Is-modified")) { //$NON-NLS-1$
			connection.writeLine("Is-modified " + file.getName()); //$NON-NLS-1$
		} else {
			sendModified(file, isBinary, monitor);
		}
	}

	/**
	 * Sends a Static-directory request to the server.
	 * <p>
	 * Indicates that the directory specified in the most recent Directory request
	 * is static.  No new files will be checked out into this directory unless
	 * explicitly requested.
	 * </p>
	 */
	public void sendStaticDirectory() throws CVSException {
		connection.writeLine("Static-directory"); //$NON-NLS-1$
	}

	/**
	 * Sends a Directory request to the server with a constructed path.
	 * <p>
	 * It may be necessary at times to guess the remote path of a directory since
	 * it does not exist yet.  In this case we construct a remote path based on the
	 * local path by prepending the local path with the repository root.  This may
	 * not work in the presence of modules, so only use it for creating new projects.
	 * </p><p>
	 * Note: A CVS repository root can end with a trailing slash. The CVS server
	 *       expects that the repository root sent contain this extra slash. Including
	 *       the foward slash in addition to the absolute remote path makes for a string
	 *       containing two consecutive slashes (e.g. /home/cvs/repo//projecta/a.txt).
	 *       This is valid in the CVS protocol.
	 * </p>
	 */
	public void sendConstructedDirectory(String localDir) throws CVSException {
		sendDirectory(localDir, getRepositoryRoot() + "/" + localDir); //$NON-NLS-1$
	}

	/**
	 * Sends a Directory request to the server.
	 * <p>e.g. sendDirectory("local_dir", "remote_dir") sends:
	 * <pre>
	 *   Directory local_dir
	 *   repository_root/remote_dir
	 * </pre></p>
	 * 
	 * @param localDir the path of the local directory relative to localRoot
	 * @param remoteDir the path of the remote directory relative to repositoryRoot
	 */
	public void sendDirectory(String localDir, String remoteDir) throws CVSException {
		if (localDir.length() == 0) localDir = "."; //$NON-NLS-1$
		connection.writeLine("Directory " + localDir); //$NON-NLS-1$
		connection.writeLine(remoteDir);
	}

	/**
	 * Sends a Directory request for the localRoot.
	 */
	public void sendLocalRootDirectory() throws CVSException {
		sendDirectory(".", localRoot.getRemoteLocation(localRoot)); //$NON-NLS-1$
	}

	/**
	 * Sends a Directory request for the localRoot with a constructed path.
	 * <p>
	 * Use this when creating a new project that does not exist in the repository.
	 * </p>
	 * @see #sendConstructedDirectory
	 */
	public void sendConstructedRootDirectory() throws CVSException {
		sendConstructedDirectory(""); //$NON-NLS-1$
	}

	/**
	 * Sends an Entry request to the server.
	 * <p>
	 * Indicates that a file is managed (but it may not exist locally).  Sends
	 * the file's entry line to the server to indicate the version that was
	 * previously checked out.
	 * </p><p>
	 * Note: The most recent Directory request must have specified the file's
	 *       parent folder.
	 * </p>
	 * 
	 * @param entryLine the formatted entry line of the managed file.
	 */
	public void sendEntry(String entryLine) throws CVSException {
		connection.writeLine("Entry " + entryLine); //$NON-NLS-1$
	}

	/**
	 * Sends a global options to the server.
	 * <p>e.g. sendGlobalOption("-n") sends:
	 * <pre>
	 *   Global_option -n \n
	 * </pre></p>
	 * 
	 * @param option the global option to send
	 */
	public void sendGlobalOption(String option) throws CVSException {
		connection.writeLine("Global_option " + option); //$NON-NLS-1$
	}

	/**
	 * Sends an Unchanged request to the server.
	 * <p>e.g. if a file called "local_file" was not modified, sends:
	 * <pre>
	 *   Unchanged local_file \n
	 * </pre></p><p>
	 * Note: The most recent Directory request must have specified the file's
	 *       parent folder.
	 * </p>
	 * 
	 * @param file the file that was not modified
	 */
	public void sendUnchanged(ICVSFile file) throws CVSException {
		connection.writeLine("Unchanged " + file.getName()); //$NON-NLS-1$
	}
	
	/**
	 * Sends a Questionable request to the server.
	 * <p>
	 * Indicates that a file exists locally but is unmanaged.  Asks the server
	 * whether or not the file should be ignored in subsequent CVS operations.
	 * The reply to the request occurs in the form of special M-type message
	 * responses prefixed with '?' when the next command is executed.
	 * </p><p>
	 * Note: The most recent Directory request must have specified the file's
	 *       parent folder.
	 * </p>
	 * 
	 * @param resource the local file or folder
	 */
	public void sendQuestionable(ICVSResource resource) throws CVSException {
		connection.writeLine("Questionable " + resource.getName()); //$NON-NLS-1$
	}

	/**
	 * Sends a Sticky tag request to the server.
	 * <p>
	 * Indicates that the directory specified in the most recent Directory request
	 * has a sticky tag or date, and sends the tag's contents.
	 * </p>
	 * 
	 * @param tag the sticky tag associated with the directory
	 */
	public void sendSticky(String tag) throws CVSException {
		connection.writeLine("Sticky " + tag); //$NON-NLS-1$
	}

	/**
	 * Sends a Modified request to the server along with the file contents.
	 * <p>e.g. if a file called "local_file" was modified, sends:
	 * <pre>
	 *   Modified local_file \n
	 *   file_permissions \n
	 *   file_size \n
	 *   [... file_contents ...]
	 * </pre></p><p>
	 * Under some circumstances, Is-modified may be used in place of this request.<br>
	 * Do not use with history, init, import, rdiff, release, rtag, or update.
	 * </p><p>
	 * Note: The most recent Directory request must have specified the file's
	 *       parent folder.
	 * </p>
	 * 
	 * @param file the file that was modified
	 * @param isBinary if true the file is sent without translating line delimiters
	 * @param monitor the progress monitor
	 * @see #sendIsModified
	 */
	public void sendModified(ICVSFile file, boolean isBinary, IProgressMonitor monitor)
		throws CVSException {
		
		String filename = file.getName();
		connection.writeLine("Modified " + filename); //$NON-NLS-1$
		ResourceSyncInfo info = file.getSyncInfo();
		if (info != null) {
			connection.writeLine(info.getPermissions());
		} else {
			// for new resources send the default permissions
			connection.writeLine(ResourceSyncInfo.getDefaultPermissions());
		}
		sendFile(file, isBinary, monitor);
	}

	/**
	 * Sends a file to the remote CVS server, possibly translating line delimiters.
	 * <p>
	 * Line termination sequences are automatically converted to linefeeds only
	 * (required by the CVS specification) when sending non-binary files.  This
	 * may alter the actual size and contents of the file that is sent.
	 * </p><p>
	 * Note: Non-binary files must be small enough to fit in available memory.
	 * </p>
	 * @param file the file to be sent
	 * @param isBinary is true if the file should be sent without translation
	 * @param monitor the progress monitor
	 */
	public void sendFile(ICVSFile file, boolean isBinary, IProgressMonitor monitor) throws CVSException {
		// check overrides
		if (textTransferOverrideSet != null &&
			textTransferOverrideSet.contains(file)) isBinary = false;

		// update progress monitor
		final String title = Policy.bind(getSendFileTitleKey(), new Object[]{ Util.toTruncatedPath(file, localRoot, 3) }); //$NON-NLS-1$
		monitor.subTask(Policy.bind("Session.transferNoSize", title)); //$NON-NLS-1$
		// obtain an input stream for the file and its size
		long size = file.getSize();
		OutputStream out = connection.getOutputStream();
		try {
			InputStream in = file.getContents();
			try {
				boolean compressed = false;
				byte[] buffer = new byte[TRANSFER_BUFFER_SIZE];
				if (! isBinary && IS_CRLF_PLATFORM || compressionLevel != 0) {
					// this affects the file size, spool the converted copy to an in-memory buffer
					if (! isBinary && IS_CRLF_PLATFORM) in = new CRLFtoLFInputStream(in);
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					OutputStream zout;
					if (compressionLevel != 0) {
						try {
							zout = new GZIPOutputStream(bout); // apparently does not support specifying compression level
							compressed = true;
						} catch (IOException e) {
							throw CVSException.wrapException(e);
						}
					} else {
						zout = bout;
					}
					for (int count; (count = in.read(buffer)) != -1;) zout.write(buffer, 0, count);
					zout.close();
					in.close();
					byte[] contents = bout.toByteArray();
					in = new ByteArrayInputStream(contents);
					size = contents.length;
				}
				// setup progress monitoring
				in = new ProgressMonitorInputStream(in, size, TRANSFER_PROGRESS_INCREMENT, monitor) {
					protected void updateMonitor(long bytesRead, long bytesTotal, IProgressMonitor monitor) {
						if (bytesRead == 0) return;
						Assert.isTrue(bytesRead <= bytesTotal);
						monitor.subTask(Policy.bind("Session.transfer", //$NON-NLS-1$
							new Object[] { title, Long.toString(bytesRead >> 10), Long.toString(bytesTotal >> 10) }));
					}
				};
				// send the file
				String sizeLine = Long.toString(size);
				if (compressed) sizeLine = "z" + sizeLine; //$NON-NLS-1$
				writeLine(sizeLine);
				for (int count; (count = in.read(buffer)) != -1;) out.write(buffer, 0, count);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
	}

	/**
	 * Receives a file from the remote CVS server, possibly translating line delimiters.
	 * <p>
	 * Line termination sequences are automatically converted to platform format
	 * only when receiving non-binary files.  This may alter the actual size and
	 * contents of the file that is received.
	 * </p><p>
	 * Translation is performed on-the-fly, so the file need not fit in available memory.
	 * </p>
	 * @param file the file to be received
	 * @param isBinary is true if the file should be received without translation
	 * @param responseType one of the ICVSFile updated types (UPDATED, CREATED, MERGED, UPDATE_EXISTING)
	 * indicating what repsonse type provided the file contents
	 * @param monitor the progress monitor
	 */
	public void receiveFile(ICVSFile file, boolean isBinary, int responseType, IProgressMonitor monitor)
	throws CVSException {
		// check overrides
		if (textTransferOverrideSet != null &&
			textTransferOverrideSet.contains(file)) isBinary = false;

		// update progress monitor
		final String title = Policy.bind("Session.receiving", new Object[]{ Util.toTruncatedPath(file, localRoot, 3) }); //$NON-NLS-1$
		monitor.subTask(Policy.bind("Session.transferNoSize", title)); //$NON-NLS-1$
		// get the file size from the server
		long size;
		boolean compressed = false;
		try {
			String sizeLine = readLine();
			if (sizeLine.charAt(0) == 'z') {
				compressed = true;
				sizeLine = sizeLine.substring(1);
			}
			size = Long.parseLong(sizeLine, 10);
		} catch (NumberFormatException e) {
			throw new CVSException(Policy.bind("Session.badInt"), e); //$NON-NLS-1$
		}
		// create an input stream that spans the next 'size' bytes from the connection
		InputStream in = new SizeConstrainedInputStream(connection.getInputStream(), size, true /*discardOnClose*/);
		// setup progress monitoring
		in = new ProgressMonitorInputStream(in, size, TRANSFER_PROGRESS_INCREMENT, monitor) {
			protected void updateMonitor(long bytesRead, long bytesTotal, IProgressMonitor monitor) {
				if (bytesRead == 0) return;
				monitor.subTask(Policy.bind("Session.transfer", //$NON-NLS-1$
					new Object[] { title, Long.toString(bytesRead >> 10), Long.toString(bytesTotal >> 10) }));
			}
		};
		// if compression enabled, decompress on the fly
		if (compressed) {
			try {
				in = new GZIPInputStream(in);
			} catch (IOException e) {
				throw CVSException.wrapException(e);
			}
		}
		// if not binary, translate line delimiters on the fly
		if (! isBinary) {
			// always auto-correct for CRLF line-ends that come from the server
			in = new CRLFtoLFInputStream(in);
			// switch from LF to CRLF if appropriate
			if (IS_CRLF_PLATFORM) in = new LFtoCRLFInputStream(in);
		}
		// write the file locally
		file.setContents(in, responseType, true, new NullProgressMonitor());
	}

	/**
	 * Stores the value of the last Mod-time response encountered.
	 * Valid only for the duration of a single CVS command.
	 */
	void setModTime(Date modTime) {
		this.modTime = modTime;
	}
	
	/**
	 * Returns the stored value of the last Mod-time response,
	 * or null if there was none while processing the current command.
	 */
	Date getModTime() {
		return modTime;
	}
	
	/**
	 * Stores true if the -n global option was specified for the current command.
	 * Valid only for the duration of a single CVS command.
	 */
	void setNoLocalChanges(boolean noLocalChanges) {
		this.noLocalChanges = noLocalChanges;
	}
	
	/**
	 * Returns true if the -n global option was specified for the current command,
	 * false otherwise.
	 */
	boolean isNoLocalChanges() {
		return noLocalChanges;
	}
	
	/**
	 * Callback hook for the ValidRequestsHandler to specify the set of valid
	 * requests for this session.
	 */
	void setValidRequests(String validRequests) {
		this.validRequests = " " + validRequests + " "; //$NON-NLS-1$  //$NON-NLS-2$
	}

	boolean isOutputToConsole() {
		return outputToConsole;
	}

	/**
	 * Stores a flag as to whether .# files will be created. (Default is true)
	 * @param createBackups if true, creates .# files at the server's request
	 */
	void setCreateBackups(boolean createBackups) {
		this.createBackups = createBackups;
	}

	/**
	 * Returns a flag as to whether .# files will be created.
	 */
	boolean isCreateBackups() {
		return createBackups;
	}

	/**
	 * Gets the sendFileTitleKey.
	 * @return Returns a String
	 */
	String getSendFileTitleKey() {
		if (sendFileTitleKey == null)
			return "Session.sending"; //$NON-NLS-1$
		return sendFileTitleKey;
	}

	/**
	 * Sets the sendFileTitleKey.
	 * @param sendFileTitleKey The sendFileTitleKey to set
	 */
	public void setSendFileTitleKey(String sendFileTitleKey) {
		this.sendFileTitleKey = sendFileTitleKey;
	}
	
	/**
	 * Remembers a set of files that must be transferred as 'text'
	 * regardless of what the isBinary parameter to sendFile() is.
	 * 
	 * @param textTransferOverrideSet the set of ICVSFiles to override, or null if none
	 */
	public void setTextTransferOverride(Collection textTransferOverrideSet) {
		this.textTransferOverrideSet = textTransferOverrideSet;
	}
}
