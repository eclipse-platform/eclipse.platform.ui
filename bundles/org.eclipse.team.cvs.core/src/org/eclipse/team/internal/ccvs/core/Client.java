package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.commands.CommandDispatcher;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.core.response.IResponseHandler;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.Util;

public class Client {

	public static final String CURRENT_LOCAL_FOLDER = ".";
	public static final String CURRENT_REMOTE_FOLDER = "";
	public static final String SERVER_SEPARATOR = "/";
	
	// Supported Commands
	public static final String CHECKOUT = "co";
	public static final String UPDATE = "update";
	public static final String COMMIT = "ci";
	public static final String ADD = "add";
	public static final String REMOVE = "remove";
	public static final String IMPORT = "import";
	public static final String TAG = "tag";
	public static final String DIFF = "diff";
	public static final String ADMIN = "admin";
	public static final String STATUS = "status";
	public static final String LOG = "log";
	
	// Global Options
	public static final String REPO_OPTION = "-d";
	public static final String NOCHANGE_OPTION = "-n";

	// Local Options	
	public static final String IGNORE_OPTION = "-I";
	public static final String WRAPPER_OPTION = "-W";
	public static final String KB_OPTION = "-kb";
	public static final String KO_OPTION = "-ko";
	public static final String PRUNE_OPTION = "-P";
	public static final String TAG_OPTION = "-r";
	public static final String BRANCH_OPTION = "-b";
	public static final String DEEP_OPTION = "-d";
	public static final String IGNORE_LOCAL_OPTION = "-d";
	public static final String LOCAL_OPTION = "-l";
	public static final String MESSAGE_OPTION = "-m";
	
	public static final String[] EMPTY_ARGS_LIST = new String[0];

	/**
	 * Executes the given command, with all the parameter. It works like a
	 * cvs-client in terms of different parameter. 
	 * Sets up the three main objects of the program:
	 * 
	 * commandDispatcher  => Knows about commands (update, commit ...)
	 * responseDispatcher => Reacts on input from the server
	 * requestSender     => Knows how to send requests to the server
	 * 
	 * @param request the cvs-command to run, not-null
	 * @param globalOptions the cvs-options null possible
	 * @param localOptions the cvs-options null possible
	 * @param arguments the cvs-arguments null possible
	 * @param mRoot the fileSystem the command is executed on, not-null
	 * @param monitor the progress-monitor null possible
	 * @param messageOut PrintStream that the Messages and Error of the server
	 * 		   are piped to
	 * @param connection the connection to the cvs-server
	 * @param customHandlers handlers for responseTypes of the server. It is 
	 * 		   dangerous to register MessageHandler and ErrorHandler. It is not
	 * 		   recomended to try to register ANY other hander.
	 * @param FirstTime if you work over an opened connection you have to set
	 * 		   the first-time parameter to true, the first time you execute a 
	 * 		   requst. After the first time you can (but you do not have to) set
	 * 		   this parameter to false in order to save the overhead of the
	 * 		   intialisation with the server
	 * @see commandDispatcher
	 * @see RequestSender
	 * @see responseDispatcher
	 */	
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						ICVSFolder mRoot,
						IProgressMonitor monitor, 
						PrintStream messageOut,
						Connection connection,
						IResponseHandler[] customHandlers,
						boolean firstTime) 
						throws CVSException {
		
		Assert.isNotNull(request);
		Assert.isNotNull(connection);
		Assert.isNotNull(mRoot);
		globalOptions = notNull(globalOptions);
		localOptions = notNull(localOptions);
		arguments = notNull(arguments);
		monitor = Policy.monitorFor(monitor);
		customHandlers = notNull(customHandlers);
		
		// We might remove certain options and arguments when 
		// we looked at them and they do not need our attention 
		// any more. This changes should not affect the caller.
		globalOptions = (String[])globalOptions.clone();
		localOptions = (String[])localOptions.clone();
		arguments = (String[])arguments.clone();
		
		ResponseDispatcher responseDispatcher = new ResponseDispatcher(connection, customHandlers);
		RequestSender requestSender = new RequestSender(connection);
		CommandDispatcher commandDispatcher = new CommandDispatcher(responseDispatcher, requestSender);
		
		if (firstTime) {
			initialize(responseDispatcher, requestSender, connection, mRoot, monitor, messageOut);
		}
		
		commandDispatcher.execute(request,
								globalOptions,
								localOptions,
								arguments,
								mRoot,
								monitor,
								messageOut);
	}

	/**
	 * Executes the given request. Give the client a CVSRepositoryLocation
	 * that the server is going to open and close the connection from.
	 * 
	 * @param repository represents an abstract cvs-repository. If it is null
	 * 		   connection-infrmation is searced in the globalOptions and in the
	 * 		   filesystem
	 * @see Client#execute(String,String[],String[],String[],ICVSFolder,IProgressMonitor,PrintStream,Connection,IResponseHandler[],boolean)
	 */
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						ICVSFolder mRoot, 
						IProgressMonitor monitor,
						PrintStream messageOut,
						CVSRepositoryLocation repository,
						IResponseHandler[] customHandlers) 
						throws CVSException {

		Assert.isNotNull(mRoot);
		globalOptions = notNull(globalOptions);

		// We might remove certain global options.
		// This should not affect the caller
		globalOptions = (String[])globalOptions.clone();
				
		if (repository == null) {
			repository = getRepository(globalOptions, mRoot);			
		}				
		
		Connection connection = repository.openConnection();
		try {
			execute(request, 
				globalOptions, 
				localOptions, 
				arguments, 
				mRoot, 
				monitor, 
				messageOut, 
				connection, 
				customHandlers,
				true);
		} finally {
			connection.close();
		}
	}
	
	/**
	 * Executes the given request in the standard cvs-way. This is the preferred 
	 * way to call the client. It is equal to the call:<br>
	 * execute(request,globalOptions,localOptions,arguments,mRoot,monitor,messageOut,null,null);<br>
	 * 
	 * @see Client#execute(String,String[],String[],String[],ICVSFolder,IProgressMonitor,PrintStream,CVSRepositoryLocation,IResponseHandler[])
	 * @see Client#execute(String,String[],String[],String[],ICVSFolder,IProgressMonitor,PrintStream,Connection,IResponseHandler[],boolean)
	 */
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						ICVSFolder mRoot,
						IProgressMonitor monitor, 
						PrintStream messageOut) 
						throws CVSException {
			
		execute(request,
			globalOptions,
			localOptions,
			arguments,
			mRoot,
			monitor,
			messageOut,
			null,
			null);
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
	
	/**
	 * Intializes the client.
	 * 
	 * Gets the valid-requsts form the server, and puts them into the
	 * request sender.
	 */
	private static void initialize(ResponseDispatcher responseDispatcher,
							RequestSender requestSender,
							Connection connection, 
							ICVSFolder mRoot, 
							IProgressMonitor monitor,
							PrintStream messageOut) 
							throws CVSException {

		// Tell the server our response handlers.
		connection.writeLine(requestSender.VALID_RESPONSES, responseDispatcher.makeResponseList());

		// Get all valid requests from the server
		connection.writeLine(requestSender.VALID_REQUESTS);
		
		// Get the responseHandler that does put the valid-requests
		// into the requestSender
		IResponseHandler validRequestHandler = requestSender.getValidRequestHandler();
		
		// Register the responseHandler, process the server-reply
		// unregister it afterwards ... we are not going to get 
		// another response of this kind
		responseDispatcher.registerResponseHandler(validRequestHandler);
		responseDispatcher.manageResponse(monitor,mRoot,messageOut);
		responseDispatcher.unregisterResponseHandler(validRequestHandler);
		
		// Set the root.
		// we just send it. If we do not send it we have got 
		// a problem anyway ... so we do not bother checking if it
		// is allowed (we could do so with "requestSender.isValidRequest(ROOT)"
		connection.writeLine(requestSender.ROOT, connection.getRootDirectory());
	}
	
	/**
	 * This give you a new repo either from the global "-d" option
	 * or form the root-property in the folder.
	 * 
	 * This has to be rewritten in a nicer style.
	 */
	private static CVSRepositoryLocation getRepository(String[] globalOptions, 
										ICVSFolder mFolder) 
										throws CVSException {
		
		String repoName = null;
		
		Assert.isNotNull(mFolder);
		
		// look if the repo is specified in the global Options
		// this delets the option as well which is not so beatyful, but
		// we have got a copy and we do not want this option to appear
		// any more
		repoName = Util.getOption(globalOptions,REPO_OPTION,true);
		
		// look if we have got an root-entrie in the root-folder
		if (repoName == null && mFolder.exists() && mFolder.isCVSFolder()) {
			repoName = mFolder.getFolderSyncInfo().getRoot();
		}
		
		if (repoName == null) {
			throw new CVSException("CVSROOT is not specified");
		}
		
		return CVSRepositoryLocation.fromString(repoName);
	}

	private static String[] notNull(String[] arg) {
		if (arg == null) {
			return new String[0];
		} else {
			return arg;
		}
	}
	
	private static IResponseHandler[] notNull(IResponseHandler[] arg) {
		if (arg == null) {
			return new IResponseHandler[0];
		} else {
			return arg;
		}
	}
}
