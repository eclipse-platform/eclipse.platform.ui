package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;

public class Session {
	public static final String CURRENT_LOCAL_FOLDER = ".";
	public static final String CURRENT_REMOTE_FOLDER = "";
	public static final String SERVER_SEPARATOR = "/";

	private Connection connection;
	private CVSRepositoryLocation location;
	private ICVSFolder localRoot;
	private String validRequests;
	private Date modTime;
	private boolean noLocalChanges;
	private boolean outputToConsole;

	/**
	 * Creates a new CVS session.
	 * 
	 * @param location
	 * @param localRoot represents the current working directory of the client
	 */
	public Session(ICVSRepositoryLocation location, ICVSFolder localRoot) {
		this(location, localRoot, true);
	}
	
	public Session(ICVSRepositoryLocation location, ICVSFolder localRoot, boolean outputToConsole) {
		this.location = (CVSRepositoryLocation) location;
		this.localRoot = localRoot;
		this.validRequests = "";
		this.outputToConsole = outputToConsole;
	}
	
	public void open(IProgressMonitor monitor) throws CVSException {
		if (connection != null) throw new IllegalStateException();
		connection = location.openConnection(monitor);
		
		// Tell the serves the names of the responses we can handle
		connection.writeLine("Valid-responses " + Command.makeResponseList());

		// Ask for the set of valid requests
		Command.VALID_REQUESTS.execute(this, Command.NO_GLOBAL_OPTIONS, Command.NO_LOCAL_OPTIONS,
			Command.NO_ARGUMENTS, null, monitor);

		// Set the root directory on the server for this connection
		connection.writeLine("Root " + location.getRootDirectory());
	}		
	
	public void close() throws CVSException {
		/// ?
		if (connection == null) throw new IllegalStateException();
		connection.close();
		connection = null;
	}
	
	public boolean isValidRequest(String request) {
		return (validRequests == null) ||
			(validRequests.indexOf(" " + request + " ") != -1);
	}

	/**
	 * Gives you an LocalFolder for a absolute path in
	 * platform dependend style.
	 * 
	 * @throws CVSException on path.indexOf("CVS") != -1
	 * @throws CVSException on internal IOExeption
	 */
	public static ICVSFolder getManagedFolder(File folder) throws CVSException {
		return new LocalFolder(folder);
	}
	public static ICVSFile getManagedFile(File file) throws CVSException {
		return new LocalFile(file);
	}
	
	public static ICVSResource getManagedResource(IResource resource) throws CVSException {
		File file = resource.getLocation().toFile();
		if (resource.getType() == IResource.FILE)
			return getManagedFile(file);
		else
			return getManagedFolder(file);
	}

	/**
	 * Returns the local root folder for this session.
	 * 
	 * @returns the local root folder
	 */
	public ICVSFolder getLocalRoot() {
		return localRoot;
	}

	/**
	 * Receives a line of text minus the newline from the server.
	 */
	public String readLine() throws CVSException {
		return connection.readLine();
	}

	/**
	 * Sends a line of text followed by a newline to the server.
	 * 
	 * @param line the line
	 */
	public void writeLine(String line) throws CVSException {
		connection.writeLine(line);
	}

	/**
	 * Sends an argument to the server.
	 * <p>
	 * e.g. sendArgument("Hello\nWorld\n  Hello World") is sent as
	 * <ul>
	 *   <li>Argument Hello</li>
	 *   <li>Argumentx World</li>
	 *   <li>Argumentx Hello World</li>
	 * </ul></p>
	 * 
	 * @param arg the argument to send
	 */
	public void sendArgument(String arg) throws CVSException {
		connection.write("Argument ");
		int oldPos = 0;
		for (;;) {
			int pos = arg.indexOf('\n', oldPos);
			if (pos == -1) break;
			connection.writeLine(arg.substring(oldPos, pos));
			connection.write("Argumentx ");
			oldPos = pos + 1;
		}
		connection.writeLine(arg.substring(oldPos));
	}

	public void sendCommand(String commandId) throws CVSException {
		connection.writeLine(commandId);
		connection.flush();
	}

	public void sendKopt(String arg) throws CVSException {
		connection.writeLine("Kopt " + arg);
	}

	public void sendIsModified(ICVSFile file) throws CVSException {
		connection.writeLine("Is-modified " + file.getName());
	}

	public void sendStaticDirectory() throws CVSException {
		connection.writeLine("Static-directory");
	}
	
	/**
	 * The Directory request is sent as:
	 * <ul>
	 * 		<li>Directory localdir
	 * 		<li>repository_root/remotedir
	 * </ul>
	 * 
	 * This note is copied from an old version:
	 * [Note: A CVS repository root can end with a trailing slash. The CVS server
	 * expects that the repository root sent contain this extra slash. Including
	 * the foward slash in addition to the absolute remote path makes for a string
	 * containing two consecutive slashes (e.g. /home/cvs/repo//projecta/a.txt).
	 * This is valid in the CVS protocol.]
	 */
	public void sendConstructedDirectory(String local, String remote) throws CVSException {
		// FIXME I do not know wether this method is "ModuleFile-safe"
		connection.writeLine("Directory " + local);
		connection.writeLine(location.getRootDirectory() + "/" + remote);
	}

	/**
	 * The Directory request is sent as:
	 * <ul>
	 * 		<li>Directory localdir
	 * 		<li>repository_root/remotedir
	 * </ul>
	 */
	public void sendDirectory(String local, String remote) throws CVSException {
		if (local.length() == 0) local = Session.CURRENT_LOCAL_FOLDER;
		connection.writeLine("Directory " + local);
		connection.writeLine(remote);
	}
	
	public void sendLocalRootDirectory() throws CVSException {
		sendDirectory(CURRENT_LOCAL_FOLDER, localRoot.getRemoteLocation(localRoot));
	}
	
	public void sendDefaultRootDirectory() throws CVSException {
		sendConstructedDirectory(Session.CURRENT_LOCAL_FOLDER, CURRENT_REMOTE_FOLDER);
	}

	
	public void sendEntry(String entryLine) throws CVSException {
		connection.writeLine("Entry " + entryLine);
	}

	public void sendGlobalOption(String option) throws CVSException {
		connection.writeLine("Global_option " + option);
	}

	public void sendUnchanged(String filename) throws CVSException {
		connection.writeLine("Unchanged " + filename);
	}

	public void sendQuestionable(String filename) throws CVSException {
		connection.writeLine("Questionable " + filename);
	}

	public void sendSticky(String tag) throws CVSException {
		connection.writeLine("Sticky " + tag);
	}

	/**
	 * This does not only send the message to the server that the
	 * file is going to be uploaded.<br>
	 * It does also acctually upload the file.<br>
	 * NOTE: The entry line has to be send before calling this method
	 */
	public void sendModified(ICVSFile file, IProgressMonitor monitor, boolean binary)
		throws CVSException {
		
		// boolean binary;
		
		// Send
		// - MODIFIED
		// - permissions
		// - size
		// - Content of the file
		
		// Does not send the entryLinde !!
		connection.writeLine("Modified " + file.getName());					
		
		ResourceSyncInfo info = file.getSyncInfo();
		if (info == null || 
			info.getPermissions() == null) {
			connection.writeLine(ResourceSyncInfo.DEFAULT_PERMISSIONS);
		} else {
			connection.writeLine(info.getPermissions());
		} 

		String progressTitle =
			Policy.bind("RequestSender.sendModified", file.getName());
		monitor.subTask(progressTitle);
		file.sendTo(connection.getOutputStream(),binary, monitor);
	}
	
	InputStream getInputStream() throws CVSException {
		return connection.getInputStream();
	}
	
	OutputStream getOutputStream() throws CVSException {
		return connection.getOutputStream();
	}
	
	public ICVSRepositoryLocation getCVSRepositoryLocation() {
		return location;
	}

	void setModTime(Date modTime) {
		this.modTime = modTime;
	}
	
	Date getModTime() {
		return modTime;
	}
	
	boolean isNoLocalChanges() {
		return noLocalChanges;
	}
	
	boolean isOutputToConsole() {
		return outputToConsole;
	}
	
	void setNoLocalChanges(boolean noLocalChanges) {
		this.noLocalChanges = noLocalChanges;
	}
	
	void setValidRequests(String validRequests) {
		this.validRequests = " " + validRequests + " ";
	}
}
