package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.core.resources.CVSFileNotFoundException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.Synchronizer;

/**
 * Abstract base class for the commands which implements the ICommand 
 * interface so subclasses can be added to the CommandDispatcher.
 * 
 * Also you do not need to use this class to implement commands
 * because the dispatcher makes use of ICommand only. However, all
 * the current command are derived from this class.
 */
public abstract class Command {
	/*** Command singleton instances ***/
	public final static Add ADD = new Add();
	public final static Admin ADMIN = new Admin();
	public final static Checkout CHECKOUT = new Checkout();
	public final static Commit COMMIT = new Commit();
	public final static Diff DIFF = new Diff();
	public final static Import IMPORT = new Import();
	public final static Log LOG = new Log();
	public final static Remove REMOVE = new Remove();
	public final static Status STATUS = new Status();
	public final static Tag TAG = new Tag();
	public final static Update UPDATE = new Update();
	final static ValidRequests VALID_REQUESTS = new ValidRequests();
	
	// Empty argument array
	public final static String[] NO_ARGUMENTS = new String[0];

	/*** Global options ***/
	// Empty global option array
	public static final GlobalOption[] NO_GLOBAL_OPTIONS = new GlobalOption[0];
	// Do not change file contents
	public static final GlobalOption DO_NOT_CHANGE = new GlobalOption("-n");
	// Do not record this operation into CVS command history
	public static final GlobalOption DO_NOT_LOG = new GlobalOption("-l");
	// Make new working files read-only
	public static final GlobalOption MAKE_READ_ONLY = new GlobalOption("-r");
	// Trace command execution
	public static final GlobalOption TRACE_EXECUTION = new GlobalOption("-t");

	/*** Global options: quietness ***/
	// Don't be quiet (normal verbosity)
	public static final QuietOption VERBOSE = new QuietOption("");
	// Be somewhat quiet (suppress informational messages)
	public static final QuietOption PARTLY_QUIET = new QuietOption("-q");
	// Be really quiet (silent but for serious problems)
	public static final QuietOption SILENT = new QuietOption("-Q");

	/*** Local options: common to many commands ***/
	// Empty local option array
	public static final LocalOption[] NO_LOCAL_OPTIONS = new LocalOption[0];
	// valid for: annotate checkout commit diff export log rdiff remove rtag status tag update  
	public static final LocalOption DO_NOT_RECURSE = new LocalOption("-l");		
	// valid for: add checkout export import update
	public static final LocalOption KSUBST_BINARY = new LocalOption("-kb");
	// valid for: checkout export update
	public static final LocalOption PRUNE_EMPTY_DIRECTORIES = new LocalOption("-P");

	/*** Response handler map ***/
	private static final Hashtable responseHandlers = new Hashtable();
	static {
		registerResponseHandler(new CheckedInHandler());
		registerResponseHandler(new CopyHandler());
		registerResponseHandler(new ModTimeHandler());
		registerResponseHandler(new RemovedHandler());
		registerResponseHandler(new RemoveEntryHandler());
		registerResponseHandler(new StaticHandler(true));
		registerResponseHandler(new StaticHandler(false));
		registerResponseHandler(new StickyHandler(true));
		registerResponseHandler(new StickyHandler(false));
		registerResponseHandler(new UpdatedHandler(true));
		registerResponseHandler(new UpdatedHandler(false));
		registerResponseHandler(new ValidRequestsHandler());		
	}
	private static void registerResponseHandler(ResponseHandler handler) {
		responseHandlers.put(handler.getResponseID(), handler);
	}
	
	/*
	 * XXX For the time being, the console listener is registered with Command
	 */
	private static ICommandOutputListener consoleListener;
	
	public static void setConsoleListener(ICommandOutputListener listener) {
		consoleListener = listener;
	}
	
	/**
	 * Prevents client code from instantiating us.
	 */
	protected Command() { }
	
	/**
	 * Returns the request string used to invoke this command on the server.
	 * [template method]
	 * 
	 * @return the command's request identifier string
	 */
	protected abstract String getCommandId();

	/**
	 */
	protected void sendArguments(Session session, String[] arguments) throws CVSException {
		for (int i = 0; i < arguments.length; ++i) {
			session.sendArgument(arguments[i]);
		}
	}

	/**
	 * Describes the local resource state to the server prior to command execution.
	 * [template method]
	 * <p>
	 * Commands must override this method to inform the server about the state of
	 * local resources using the Entries, Modified, Unchanged, and Questionable
	 * requests as needed.
	 * </p>
	 * 
	 * @param session the CVS session
	 * @param globalOptions the global options for the command
	 * @param localOptions the local options for the command
	 * @param resources the resource arguments for the command
	 * @param monitor the progress monitor
	 */
	protected abstract void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException;

	/**
	 * Cleans up after command execution.
	 * [template method] 
	 * <p>
	 * The default implementation is a no-op.  Subclasses may override this
	 * method to follow up command execution on the server with clean up
	 * operations on local resources.
	 * </p>
	 *
	 * @param session the CVS session
	 * @param globalOptions the global options for the command
	 * @param localOptions the local options for the command
	 * @param resources the resource arguments for the command
	 * @param monitor the progress monitor
	 * @param serverError true iff the server returned the "ok" response
	 */
	protected void commandFinished(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor,
		boolean serverError) throws CVSException {
	}

	/**
	 * Sends the local working directory path prior to command execution.
	 * [template method]
	 * <p>
	 * The default implementation sends the paths of local root directory
	 * (assuming it exists).  Subclasses may override this method to provide
	 * alternate behaviour.
	 * </p>
	 * 
	 * @param session the CVS session
	 */
	protected void sendLocalWorkingDirectory(Session session) throws CVSException {
		ICVSFolder localRoot = session.getLocalRoot();
		if (localRoot.isCVSFolder()) {
			session.sendLocalRootDirectory();
		} else {
			session.sendDefaultRootDirectory();
		}
	}

	/**
	 * Computes an array of ICVSResources corresponding to command arguments.
	 * [template method]
	 * <p>
	 * The default implementation assumes that all arguments supplied to the
	 * command represent resources in the local root so that these resources
	 * can be more easily manipulated.  Subclasses must override this method
	 * if this assumption is false.
	 */
	protected ICVSResource[] computeWorkResources(Session session, String[] arguments)
		throws CVSException {
		ICVSFolder localRoot = session.getLocalRoot();

		if (arguments.length == 0) {
			// As a convenience, passing no arguments to the CVS command
			// implies the command will operate on the local root folder.
			return new ICVSResource[] { localRoot };
		} else {
			// Assume all arguments represent resources that are descendants
			// of the local root folder.
			ICVSResource[] resources = new ICVSResource[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				try {
					resources[i] = localRoot.getChild(arguments[i]);
				} catch (CVSFileNotFoundException e) {
					// XXX Temporary fix to allow non-managed resources to be used as arguments
					resources[i] = localRoot.getFile(arguments[i]);
				}
			}
			return resources;
		}
	}

	/**
	 * Send an array of Resources.
	 * 
	 * @see Command#sendFileStructure(ICVSResource,IProgressMonitor,boolean,boolean,boolean)
	 */
	protected void sendFileStructure(Session session, ICVSResource[] resources,
		boolean modifiedOnly, boolean emptyFolders, IProgressMonitor monitor) throws CVSException {
		checkArgumentsManaged(resources);
		FileStructureVisitor fsVisitor = new FileStructureVisitor(session, modifiedOnly, emptyFolders,  monitor);
		for (int i = 0; i < resources.length; i++) {
			resources[i].accept(fsVisitor);
		}
	}

	/**
	 * Checks that all the workResources are managed Resources.
	 * (For folders we check isCVSFolder, because of a project-folder
	 * that is not managed, because it is not registerd in the 
	 * parent-folder<br>
	 * 
	 * @throws CVSException if not all the arguments are
	 *          managed
	 */
	protected void checkArgumentsManaged(ICVSResource[] mWorkResources) throws CVSException {

		for (int i = 0; i < mWorkResources.length; i++) {
			if (mWorkResources[i].isFolder()) {
				if (!((ICVSFolder) mWorkResources[i]).isCVSFolder()) {
					throw new CVSException("Argument " + mWorkResources[i].getName() + "is not managed");
				}
			} else {
				if (!mWorkResources[i].getParent().isCVSFolder()) {
					throw new CVSException("Argument " + mWorkResources[i].getParent() + "is not managed");
				}
			}
		}
	}
	
	/**
	 * Reload the sync info for all arguments this command will be running on.
	 */
	private void reloadSyncInfo(ICVSResource[] resources, IProgressMonitor monitor) throws CVSException {
		try {
			monitor = Policy.monitorFor(monitor);
			monitor.beginTask("", 100 * resources.length);
			for (int i = 0; i < resources.length; i++) {
				Synchronizer.getInstance().reload(resources[i], Policy.subMonitorFor(monitor, 100));				
			}
		} finally {
			monitor.done();
		}
	}
	
	/**
	 */
	public IStatus execute(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, String[] arguments, ICommandOutputListener listener,
		IProgressMonitor monitor)
		throws CVSException {
		try {
			session.setNoLocalChanges(DO_NOT_CHANGE.isElementOf(globalOptions));
			session.setModTime(null);

			monitor = Policy.monitorFor(monitor);
			monitor.beginTask(Policy.bind("Command.server"), 100);
			Policy.checkCanceled(monitor);
	
			// Ensure that the commands run with the latest contents of the CVS subdirectory sync files 
			// and not the cached values. Allow 10% of work.
			ICVSResource[] resources = computeWorkResources(session, arguments);
			reloadSyncInfo(resources, Policy.subMonitorFor(monitor, 10));
			Policy.checkCanceled(monitor);
	
			/*** initiate command ***/
			// send global options
			for (int i = 0; i < globalOptions.length; ++i) {
				globalOptions[i].send(session);
			}
			// send local options
			for (int i = 0; i < localOptions.length; ++i) {
				localOptions[i].send(session);
			}
			// send local working directory state
			sendLocalResourceState(session, globalOptions, localOptions,
				resources, Policy.subMonitorFor(monitor, 10));
			Policy.checkCanceled(monitor);
			// send local working directory path
			sendLocalWorkingDirectory(session);
			// send arguments
			sendArguments(session, arguments);
			
			// send command
			session.sendCommand(getCommandId());

			/*** process responses ***/
			// Processing responses contributes 70% of work.
			IStatus status = processResponses(session, listener, Policy.subMonitorFor(monitor, 70));

			// Finished adds last 10% of work.
			commandFinished(session, globalOptions, localOptions, resources, monitor,
				status.getCode() != CVSException.SERVER_ERROR);
			monitor.worked(10);
			return status;
		} finally {
			// This will automatically persist any changes that were made to the
			// sync info while running a command.
			Synchronizer.getInstance().save(monitor);
			monitor.done();
		}
	}
	
	/**
	 * 
	 */
	private IStatus processResponses(Session session, ICommandOutputListener listener,
		IProgressMonitor monitor) throws CVSException {
		// This number can be tweaked if the monitor is judged to move too
		// quickly or too slowly. After some experimentation this is a good
		// number for both large projects (it doesn't move so quickly as to
		// give a false sense of speed) and smaller projects (it actually does
		// move some rather than remaining still and then jumping to 100).
		final int TOTAL_WORK = 300;
		monitor.beginTask(Policy.bind("Command.receivingResponses"), TOTAL_WORK);
		int halfWay = TOTAL_WORK / 2;
		int currentIncrement = 4;
		int nextProgress = currentIncrement;
		int worked = 0;

		MultiStatus accumulatedStatus = new MultiStatus(CVSProviderPlugin.ID, CVSException.OK,
			Policy.bind("Command.succeeded"), null);
		for (;;) {
			// update monitor work amount
			if (--nextProgress <= 0) {
				monitor.worked(1);
				worked++;
				if (worked >= halfWay) {
					// we have passed the current halfway point, so double the
					// increment and reset the halfway point.
					currentIncrement *= 2;
					halfWay += (TOTAL_WORK - halfWay) / 2;				
				}
				// reset the progress counter to another full increment
				nextProgress = currentIncrement;
			}			
			Policy.checkCanceled(monitor);

			// retrieve a response line
			String response = session.readLine();
			int spacePos = response.indexOf(' ');
			String argument;
			if (spacePos != -1) {
				argument = response.substring(spacePos + 1);
				response = response.substring(0, spacePos);
			} else argument = "";

			// handle completion responses
			if (response.equals("ok")) {
				break;
			} else if (response.equals("error")) {
				MultiStatus status = new MultiStatus(CVSProviderPlugin.ID,
					CVSException.SERVER_ERROR, argument, null);
				status.merge(accumulatedStatus);
				return status;
			// handle message responses
			} else if (response.equals("M")) {
				if (consoleListener != null && session.isOutputToConsole()) consoleListener.messageLine(argument, null, null);
				if (listener != null) {
					IStatus status = listener.messageLine(argument, session.getLocalRoot(), monitor);
					accumulatedStatus.merge(status);
				}
			} else if (response.equals("E")) {
				if (consoleListener != null && session.isOutputToConsole()) consoleListener.errorLine(argument, null, null);
				if (listener != null) {
					IStatus status = listener.errorLine(argument, session.getLocalRoot(), monitor);
					accumulatedStatus.merge(status);
				}
			// handle other responses
			} else {
				ResponseHandler handler = (ResponseHandler) responseHandlers.get(response);
				if (handler != null) {
					handler.handle(session, argument, monitor);
				} else {
					throw new CVSException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
						CVSProviderPlugin.ID, CVSException.IO_FAILED,
						Policy.bind("Command.unsupportedResponse", response, argument), null));
				}
			}
		}
		return accumulatedStatus;
	}
	
	/**
	 * Makes a list of all valid responses; for initializing a session.
	 * @return a space-delimited list of all valid response strings
	 */
	static String makeResponseList() {
		StringBuffer result = new StringBuffer("ok error M E");		
		Iterator elements = responseHandlers.keySet().iterator();
		while (elements.hasNext()) {
			result.append(' ');
			result.append((String) elements.next());
		}
		
		return result.toString();
	}

	/**
	 * Superclass for all CVS command options
	 */
	protected static abstract class Option {
		protected String option, argument;
		protected Option(String option, String argument) {
			this.option = option;
			this.argument = argument;
		}
		/*
		 * Returns the text string representing this option
		 * @return the option
		 *
		public String getOption() {
			return option;
		}*/
		/*
		 * Returns the (optional) argument for this option
		 * @return the argument, or null if none provided
		 *
		public String getArgument() {
			return argument;
		}*/
		/**
		 * Determines if this option is an element of an array of options
		 * @param array the array of options
		 * @return true iff the array contains this option
		 */
		public boolean isElementOf(Option[] array) {
			return findOption(array, option) != null;
		}
		/**
		 * Sends the option to a CVS server
		 * @param session the CVS session
		 */
		public abstract void send(Session session) throws CVSException;		
	}	
	/**
	 * Option subtype for global options that are common to all commands.
	 */
	public static class GlobalOption extends Option {
		protected GlobalOption(String option) {
			super(option, null);
		}
		public void send(Session session) throws CVSException {
			session.sendGlobalOption(option);
		}
	}
	/**
	 * Option subtype for global quietness options.
	 */
	public static final class QuietOption extends GlobalOption {
		private QuietOption(String option) {
			super(option);
		}
		public void send(Session session) throws CVSException {
			if (option.length() != 0) super.send(session);
		}
	}
	/**
	 * Option subtype for local options that vary from command to command.
	 */
	public static class LocalOption extends Option {
		protected LocalOption(String option) {
			super(option, null);
		}
		protected LocalOption(String option, String argument) {
			super(option, argument);
		}
		public void send(Session session) throws CVSException {
			session.sendArgument(option);
			if (argument != null) session.sendArgument(argument);
		}
	}

	/**
	 * Makes a -m log message option.
	 * Valid for: add commit import
	 */
	public static LocalOption makeMessageOption(String message) {
		return new LocalOption("-m", message);
	}
	
	/**
	 * Makes a -r or -D option for a tag.
	 * Valid for: checkout export history rdiff update
	 */
	public static LocalOption makeTagOption(CVSTag tag) {
		int type = tag.getType();
		switch (type) {
			case CVSTag.BRANCH:
			case CVSTag.VERSION:
				return new LocalOption("-r", tag.getName());
			case CVSTag.DATE:
				return new LocalOption("-D", tag.getName());
			default:
				// tag must not be HEAD
				throw new IllegalArgumentException("Sticky tag not " +
					"valid for trunk (HEAD).");
		}
	}

	/**
	 * Find a specific option in an array of options
	 * @param array the array of options
	 * @param option the option string to search for
	 * @return the first element matching the option string, or null if none
	 */
	protected static Option findOption(Option[] array, String option) {
		for (int i = 0; i < array.length; ++i) {
			// FIXME: can be optimized using identity
			if (array[i].option.equals(option)) return array[i];
		}
		return null;
	}

	/**
	 * Collect all arguments of a specific option from an array of options
	 * @param array the array of options
	 * @param option the option string to search for
	 * @return an array of all arguments of belonging to matching options
	 */
	protected static String[] collectOptionArguments(Option[] array, String option) {
		Vector /* of String */ list = new Vector();
		for (int i = 0; i < array.length; ++i) {
			if (array[i].option.equals(option)) {
				list.add(array[i].argument);
			}
		}
		return (String[]) list.toArray(new String[list.size()]);
	}
}