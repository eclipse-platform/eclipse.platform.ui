package org.eclipse.team.internal.ccvs.core.requests;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.StringTokenizer;

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.response.IResponseHandler;

/**
 * The reqest-sender is the only way to send messages to the 
 * server.
 * 
 * It has a lot of helper-methods like "sendGlobalOption, sendEntry ..."
 * this do send messages with parameter up to the server. These ways of
 * sending are discibed in the cvs-protocol specification
 * 
 */
public class RequestSender {

	/** Requests that don't expect any response from the server */
	public static final String ARGUMENT = "Argument";
	public static final String ARGUMENTX = "Argumentx";
	public static final String DIRECTORY = "Directory";
	public static final String ENTRY = "Entry";
	public static final String GLOBAL_OPTION = "Global_option";
	public static final String ROOT = "Root";
	public static final String UNCHANGED = "Unchanged";
	public static final String VALID_RESPONSES = "Valid-responses";
	public static final String QUESTIONABLE = "Questionable";
	public static final String KOPT = "Kopt";
	public static final String STATIC_DIRECTORY = "Static-directory";
	public static final String STICKY = "Sticky";
	public static final String MODIFIED = "Modified";
	public static final String IS_MODIFIED = "Is-modified";
	
	/** Requests that do expect any response from the server */
	public static final String CHECKOUT = "co";
	public static final String IMPORT = "import";
	public static final String VALID_REQUESTS = "valid-requests";
	public static final String EXPAND_MODULES = "expand-modules";
	public static final String CI = "ci";
	public static final String STATUS = "status";
	public static final String UPDATE = "update";
	public static final String HISTORY = "history";
	public static final String ADD = "add";
	public static final String REMOVE = "remove";
	public static final String LOG = "log";
	public static final String RTAG = "rtag";
	public static final String TAG = "tag";
	public static final String DIFF = "diff";
	public static final String ADMIN = "admin";
	
	/** Helper Constants that are not going to be send to server */
	public static final String SERVER_SEPERATOR = "/";
	private static final String EMPTY_LOCAL_FOLDER = ".";
	private static final String LINEFEED = "\n";
	private static final String CRETURN = "\r";
	private static final String STANDARD_PERMISSION = "u=rw,g=rw,o=r";

	/** 
	 * The link to the server to send things out
	 */
	private Connection connection;
	
	/**
	 * List of the valid-request as stated from the 
	 * server.
	 * For future checking on that.
	 */
	private String validRequests;

	/**
	 * Constructor that takes the connection
	 */
	public RequestSender (Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Is the given request a valid server request.
	 */
	public boolean isValidRequest(String requestName) {
		if (validRequests == null)
			return false;
		return validRequests.indexOf(requestName) != -1;
	}
	
	/**
	 * Set the list of valid-request when you get 
	 * the list of valid request from the server.
	 */
	void setValidRequest(String validRequests) {
		this.validRequests = validRequests;
	}
	
	/**
	 * Get a Handler for the "valid-request", that does 
	 * collect the information to this class.
	 */
	public IResponseHandler getValidRequestHandler() {
		return new ValidRequestHandler(this);
	}
	
	/**
	 * This is the general way to send text to the server.
	 * Most commonly it is used to send a single constant
	 * to the server
	 */
	public void writeLine(String data) throws CVSException {
		connection.writeLine(data);
	}
	
	/**
	 * Sends an argument to the server. If arg contains newlines
	 * of any kind the argument as one first argument and after 
	 * that as argument extentions.<br>
	 * E.g.: sendArgument("Hello\nWorld\nHello\r  World") is send as
	 * <ul>
	 * <li> Argument Hello
	 * <li> Argumentx World
	 * <li> Argumentx Hello
	 * <li> Argumentx   World
	 * </ul>
	 */
	public void sendArgument(String arg) throws CVSException {
		
		StringTokenizer tokenizer;
		
		if (arg.indexOf(LINEFEED) == -1 && 
			arg.indexOf(CRETURN) == -1) {
			connection.writeLine(ARGUMENT, arg);
			return;
		} 
		
		// Create a tokenizer, that uses all newline-caracters as 
		// delimitor
		tokenizer = new StringTokenizer(arg,LINEFEED + CRETURN);
		
		// We do not want an argument with a newlines only
		Assert.isTrue(tokenizer.hasMoreTokens());

		connection.writeLine(ARGUMENT, tokenizer.nextToken());
		while (tokenizer.hasMoreTokens()) {
			connection.writeLine(ARGUMENTX, tokenizer.nextToken());
		}
	}

	public void sendKopt(String arg) throws CVSException {
		connection.writeLine(KOPT, arg);
	}

	public void sendIsModified(String file) throws CVSException {
		connection.writeLine(IS_MODIFIED, file);
	}

	public void sendStaticDirectory() throws CVSException {
		connection.writeLine(STATIC_DIRECTORY);
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
		
		connection.writeLine(DIRECTORY, local);
		connection.writeLine(connection.getRootDirectory() + 
							SERVER_SEPERATOR + remote);
	}

	/**
	 * The Directory request is sent as:
	 * <ul>
	 * 		<li>Directory localdir
	 * 		<li>repository_root/remotedir
	 * </ul>
	 */
	public void sendDirectory(String local, String remote) throws CVSException {
		
		if (local.equals("")) {
			local = EMPTY_LOCAL_FOLDER;
		}
		
		connection.writeLine(DIRECTORY, local);
		connection.writeLine(remote);
	}
	
	public void sendEntry(String entryLine) throws CVSException {
		connection.writeLine(ENTRY, entryLine);
	}

	public void sendGlobalOption(String option) throws CVSException {
		connection.writeLine(GLOBAL_OPTION, option);
	}

	public void sendUnchanged(String filename) throws CVSException {
		connection.writeLine(UNCHANGED, filename);
	}

	public void sendQuestionable(String filename) throws CVSException {
		connection.writeLine(QUESTIONABLE, filename);
	}

	public void sendSticky(String tag) throws CVSException {
		connection.writeLine(STICKY, tag);
	}

	/**
	 * This does not only send the message to the server that the
	 * file is going to be uploaded.<br>
	 * It does also acctually upload the file.<br>
	 * NOTE: The entry line has to be send before calling this method
	 */
	public void sendModified(IManagedFile file, IProgressMonitor monitor, boolean binary)
		throws CVSException {
		
		// boolean binary;
		
		// Send
		// - MODIFIED
		// - permissions
		// - size
		// - Content of the file
		
		// Does not send the entryLinde !!
		connection.writeLine(MODIFIED, file.getName());					
		
		if (file.getFileInfo() == null || 
			file.getFileInfo().getPermissions() == null) {
			connection.writeLine(STANDARD_PERMISSION);
		} else {
			connection.writeLine(file.getFileInfo().getPermissions());
		} 

		String progressTitle =
			Policy.bind("RequestSender.sendModified", file.getName());
		monitor.subTask(progressTitle);
		file.sendTo(connection.getRequestStream(),monitor,binary);
	}

	
}
