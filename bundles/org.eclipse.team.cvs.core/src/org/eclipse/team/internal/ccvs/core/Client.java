package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.io.PrintStream;

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.internal.ccvs.core.commands.CommandDispatcher;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.ResourceFactory;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.response.IResponseHandler;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * An generic cvs client that can execute a request and can handle
 * responses. It is called like the according to the specification 
 * of cvs-command line clients. You have to give the execute method
 * the command, all the parameters, a stream to pipe the messages from
 * the server to and a monitor.<br>
 * After the client has established a connection to a server it
 * uses the client / server negotiation protcol to tell the server
 * which response can be handled by the client. Additionally the
 * server tells the client which requests can the handle by the
 * server. The list of responses that can be handled by the client
 * is determined by the registered handlers.<br>
 * <p>
 * Although not documented, the client must have a handler for at
 * least the following responses: "ok", "error", "Checked-in",
 * "Updated", "Merged", "Removed", "M" text and "E" text.
 * <p>
 * The client installs handlers for all must responses.
 */
public class Client {

	public static final String CURRENT_LOCAL_FOLDER = ".";
	public static final String CURRENT_REMOTE_FOLDER = "";
	public static final String SERVER_SEPARATOR = "/";
	
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
	 * Executes the given request
	 */
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						IManagedFolder localFolder, 
						IProgressMonitor monitor,
						PrintStream messageOut,
						CVSRepositoryLocation repository,
						IResponseHandler[] customHandlers) 
						throws CVSException {
		Connection connection = repository.openConnection();
		try {
			execute(request, globalOptions, localOptions, arguments, localFolder, monitor, messageOut, connection, customHandlers);
		} finally {
			connection.close();
		}
	}
	
	/**
	 * Executes the given request.
	 * 
	 * create a new Connection to the server, either from an "-d" global option
	 * or from the Root-Properie of the root-folder.
	 */
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						IManagedFolder mRoot,
						IProgressMonitor monitor, 
						PrintStream messageOut) 
						throws CVSException {
		execute(request, globalOptions, localOptions, arguments, mRoot, monitor, messageOut, new IResponseHandler[0]);
	}
							
	/**
	 * Executes the given request.
	 * 
	 * create a new Connection to the server, either from an "-d" global option
	 * or from the Root-Properie of the root-folder.
	 */
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						IManagedFolder mRoot,
						IProgressMonitor monitor, 
						PrintStream messageOut,
						IResponseHandler[] customHandlers) 
						throws CVSException {
		
		CVSRepositoryLocation repository;
		Connection connection;

		// this is a hack to prevent from changing the
		// acctuall array that is given from the user
		// while nulling the "-d" ":pserver:nkra@ ... " option
		globalOptions = (String[]) globalOptions.clone();
		
		repository = getRepository(globalOptions, mRoot);
		connection = repository.openConnection();

		try {		
			execute(request,
				globalOptions,
				localOptions,
				arguments,
				mRoot,
				monitor,
				messageOut,
				connection,
				customHandlers);
		} finally {
			connection.close();
		}
			
	}

	/**
	 * Executes the given request.
	 * 
	 * Uses a connection to the server that is allready there. This
	 * expects you not to use the global option "-d".
	 * 
	 * Will this ever close the connection? If so, what are the conditions
	 */
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						IManagedFolder mRoot,
						IProgressMonitor monitor, 
						PrintStream messageOut,
						Connection connection) 
						throws CVSException {
		
		execute(request,
				globalOptions,
				localOptions,
				arguments,
				mRoot,
				monitor,
				messageOut,
				connection,
				null);
	}
	
	/**
	 * Creates a new client connection for the given cvs repository location.
	 * 
	 * Sets up the three main objects of the program:
	 * 
	 * commandDispatcher  => Knows about commands (update, commit ...)
	 * responseDispatcher => Reacts on input from the server
	 * requestSender     => Knows how to send requests to the server
	 * 
	 * @see commandDispatcher
	 * @see RequestSender
	 * @see responseDispatcher
	 * 
	 */
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						IManagedFolder mRoot,
						IProgressMonitor monitor, 
						PrintStream messageOut,
						Connection connection,
						IResponseHandler[] customHandlers) 
						throws CVSException {
				
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
	}
	
		/**
	 * Creates a new client connection for the given cvs repository location.
	 * 
	 * Sets up the three main objects of the program:
	 * 
	 * commandDispatcher  => Knows about commands (update, commit ...)
	 * responseDispatcher => Reacts on input from the server
	 * requestSender     => Knows how to send requests to the server
	 * 
	 * @see commandDispatcher
	 * @see RequestSender
	 * @see responseDispatcher
	 * 
	 */
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						IManagedFolder mRoot,
						IProgressMonitor monitor, 
						PrintStream messageOut,
						Connection connection,
						IResponseHandler[] customHandlers,
						boolean firstTime) 
						throws CVSException {
				
		ResponseDispatcher responseDispatcher = new ResponseDispatcher(connection, customHandlers);
		RequestSender requestSender = new RequestSender(connection);
		CommandDispatcher commandDispatcher = new CommandDispatcher(responseDispatcher, requestSender);
		
		if (firstTime)
			initialize(responseDispatcher, requestSender, connection, mRoot, monitor, messageOut);

		commandDispatcher.execute(request,
								globalOptions,
								localOptions,
								arguments,
								mRoot,
								monitor,
								messageOut);
	}
	
	/**
	 * @see Client#(String,String[],String[],String[],ICVSFolder,IProgressMonitor,OutputStream,Connection) 
	 */
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						ICVSFolder root,
						IProgressMonitor monitor, 
						PrintStream messageOut) 
						throws CVSException {
		execute(request,
				globalOptions,
				localOptions,
				arguments,
				ResourceFactory.getManaged(root),
				monitor,
				messageOut);
	}

	/**
	 * @see Client#(String,String[],String[],String[],ICVSFolder,IProgressMonitor,OutputStream,Connection) 
	 */
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						ICVSFolder root,
						IProgressMonitor monitor, 
						PrintStream messageOut,
						Connection connection) 
						throws CVSException {
		execute(request,
				globalOptions,
				localOptions,
				arguments,
				ResourceFactory.getManaged(root),
				monitor,
				messageOut,
				connection);
	}

	/**
	 * @see Client#(String,String[],String[],String[],ICVSFolder,IProgressMonitor,OutputStream,Connection) 
	 */
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						File root,
						IProgressMonitor monitor, 
						PrintStream messageOut) 
						throws CVSException {
		execute(request,
				globalOptions,
				localOptions,
				arguments,
				ResourceFactory.getManagedFolder(root),
				monitor,
				messageOut);
	}

	/**
	 * @see Client#(String,String[],String[],String[],ICVSFolder,IProgressMonitor,OutputStream,Connection) 
	 */
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						File root,
						IProgressMonitor monitor, 
						PrintStream messageOut,
						Connection connection) 
						throws CVSException {
		execute(request,
				globalOptions,
				localOptions,
				arguments,
				ResourceFactory.getManagedFolder(root),
				monitor,
				messageOut,
				connection);
	}
	
	/**
	 * Gives you an ManagedFolder for a absolut path in
	 * platform dependend style
	 * 
	 * @throws CVSException on path.indexOf("CVS") != -1
	 * @throws CVSException on internal IOExeption
	 */
	public static IManagedFolder getManagedFolder(String folder) throws CVSException {
		return ResourceFactory.getManagedFolder(folder);
	}
	public static IManagedFolder getManagedFolder(File folder) throws CVSException {
		return ResourceFactory.getManagedFolder(folder);
	}
	public static IManagedResource getManagedResource(File file) throws CVSException {
		return ResourceFactory.getManaged(file);
	}
	
	/**
	 * Intializes the client.
	 * 
	 * Gets the valid-requsts form the server, and puts them into the
	 * request sender.
	 * 
	 * MV: Why isn't this method in the Command execute method?
	 */
	private static void initialize(ResponseDispatcher responseDispatcher,
							RequestSender requestSender,
							Connection connection, 
							IManagedFolder mRoot, 
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
										IManagedFolder mFolder) 
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
			repoName = mFolder.getFolderInfo().getRoot();
		}
		
		if (repoName == null) {
			throw new CVSException("CVSROOT is not specified");
		}
		
		return CVSRepositoryLocation.fromString(repoName);
	}		
}
