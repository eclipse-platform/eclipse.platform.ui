package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * Abstract base class for the commands which implements the ICommand 
 * interface so subclasses can be added to the CommandDispatcher.
 * 
 * Also you do not need to use this class to implement commands
 * because the dispatcher makes use of ICommand only. However, all
 * the current command are derived from this class.
 */
abstract class Command implements ICommand {
	
	private String[] globalOptions;
	private String[] localOptions;
	private String[] arguments;
	
	private IManagedFolder mRoot;
	
	protected final ResponseDispatcher responseDispatcher;
	protected final RequestSender requestSender;
	
	/**
	 * The CommandDispatcher, the ResponseDispatcher and
	 * the RequestSender are the three major objects in
	 * the client.
	 * 
	 * ResponseDispatcher is used to process the response form the server.
	 * RequestSender is used to send requests to the server.
	 */
	public Command(ResponseDispatcher responseDispatcher,
					RequestSender requestSender) {
						
		this.responseDispatcher = responseDispatcher;
		this.requestSender = requestSender;
	}
	
	/**
	 * Execute the given command. Do so by invoking the sendRequestsToServer method.
	 * Does handle the work with the progress-monitor.
	 * 
	 * @see ICommand#execute(Connection, String[], String[], ICVSResource, OutputStream)
	 */
	public void execute (
		String[] globalOptions, 
		String[] localOptions, 
		String[] arguments, 
		IManagedFolder mRoot,
		IProgressMonitor monitor, 
		PrintStream messageOut) 
			throws CVSException {
		
		// Record the arguments so subclass can access them using the get methods 
		this.mRoot = mRoot;
		this.globalOptions = globalOptions;
		this.localOptions = localOptions;
		this.arguments = arguments;
		
		try {
			
			monitor.beginTask(Policy.bind("Command.server"), 100);			
			Policy.checkCanceled(monitor);
			
			// Send the options to the server (the command itself has to care
			// about the arguments)
			// It is questionable if this is going to stay here, because
			// NOTE: because why?
			sendGlobalOptions();
			sendLocalOptions();
			
			// Guess that set up contributes 20% of work.
			sendRequestsToServer(Policy.subMonitorFor(monitor, 20));
			Policy.checkCanceled(monitor);
			
			// Send all arguments to the server
			sendArguments();
			// Send the request name to the server
			requestSender.writeLine(getRequestName());
			
			try {
				// Processing responses contributes 70% of work.
				responseDispatcher.manageResponse(Policy.subMonitorFor(monitor, 70), mRoot, messageOut);

			} catch (CVSException e) {
				finished(false);
				throw e;
			}
			// Finished adds last 10% of work.
			finished(true);
			monitor.worked(10);
		} finally {
			monitor.done();
		}

	}
	
	/**
	 * Abstract method to send the complete arguments of the command to the server.
	 * The command itself is not sent here but in the execute method.
	 */
	protected abstract void sendRequestsToServer(IProgressMonitor monitor) throws CVSException;

	/**
	 * Called after command has been executed to allow subclasses to cleanup.
	 * Default is to do nothing.
	 */
	protected void finished(boolean success) throws CVSException {
	}

	/**
	 * Sends the arguments to the server.
	 */
	protected void sendArguments() throws CVSException {
		if (arguments == null) {
			return;
		}
		for (int i= 0; i < arguments.length; i++) {
			requestSender.sendArgument(arguments[i]);
		}
	}

	/**
	 * Sends localOptions to the server.
	 */
	protected void sendLocalOptions() throws CVSException {
		if (localOptions == null)
			return;
		for (int i= 0; i < localOptions.length; i++) {
			requestSender.sendArgument(localOptions[i]);
		}
	}
	
	/**
	 * Sends the global options to the server.
	 * 
	 * It is allowed for the globalOptions to have null-values so this
	 * method has to cope with null-values in the array. Also, the 
	 * global options may be null at all.
	 */
	protected void sendGlobalOptions() throws CVSException {
		if (globalOptions == null) {
			return;
		}
		for (int i= 0; i < globalOptions.length; i++) {
			if (globalOptions[i] != null) {
				requestSender.sendGlobalOption(globalOptions[i]);
			}
		}
	}
	
	/**
	 * Send the homefolder as last thing before you send (eventually the
	 * arguments and then) the command.
	 * 
	 * lookLocal specifies whether the system tries to look into the 
	 * CVS properties for the folder.
	 */
	protected void sendHomeFolder(boolean lookLocal) throws CVSException {
		if (lookLocal && mRoot.isCVSFolder()) {
			requestSender.sendDirectory(Client.CURRENT_LOCAL_FOLDER, mRoot.getRemoteLocation(mRoot));		
		} else {
			requestSender.sendConstructedDirectory(Client.CURRENT_LOCAL_FOLDER, Client.CURRENT_REMOTE_FOLDER);
		}		
	}

	/**
	 * Send the homefolder as last thing before you send (eventually the
	 * arguments and then) the command
	 */
	protected void sendHomeFolder() throws CVSException {
		sendHomeFolder(true);
	}

	/**
	 * Gets the getGlobalOptions
	 * @return Returns a String[]
	 */
	protected String[] getGlobalOptions() {
		return globalOptions;
	}
	
	/**
	 * Gets the arguments
	 * @return Returns a String[]
	 */
	protected String[] getArguments() {
		return arguments;
	}

	/**
	 * Gets the localOptions
	 * @return Returns a String[]
	 */
	protected String[] getLocalOptions() {
		return localOptions;
	}
	
	/**
	 * getRoot returns the folder the client was called with. 
	 * (Sometimes that is not the folder you want to work with)
	 * 
	 * @return Returns a ICVSResource
	 */
	protected IManagedFolder getRoot() throws CVSException {
		
		if (!mRoot.isFolder()) {
			throw new CVSException(Policy.bind("Command.invalidRoot", new Object[] {mRoot.toString()}));
		}
		
		return mRoot;
	}
	
	/**
	 * Takes all the arguments and gives them back as resources from the
	 * root. This represents all the resources the client should work on.
	 * 
	 * If there are no arguments gives the root folder back only.
	 */
	protected IManagedResource[] getWorkResources() throws CVSException {
		return getWorkResources(0);
	}
	
	/**
	 * Work like getWorkResources() but do not look at the first 
	 * skip elements when creating the resources (this is useful when
	 * the first skip arguments of a command are not files but something
	 * else)
	 * 
	 * @see Command#getWorkResources()
	 */
	protected IManagedResource[] getWorkResources(int skip) throws CVSException {
		
		IManagedResource[] result;
		
		Assert.isTrue(arguments.length >= skip);
		
		if (arguments.length == skip) {
			return new IManagedResource[]{mRoot};
		}
		
		result = new IManagedResource[arguments.length - skip];
		
		for (int i = skip; i<arguments.length; i++) {
			result[i - skip] = mRoot.getChild(arguments[i]);
		}
		
		return result;
	}
	
	/**
	 * Get the resource that you are working with. This is a folder
	 * most of the time, but could be a file on some operations as 
	 * well.
	 * 
	 * It does also garantee that the WorkResource is a cvsFolder,
	 * or (if it is a file) does live in a cvsFolder. 
	 * 
	 * This does not apply to every operation (e.g. would not work on a 
	 * checkout)
	 * 
	 * @deprecated
	 */
	protected IManagedResource getWorkResource(String relativeFolderPath) throws CVSException {
		
		IManagedResource workResource;
		IManagedFolder contextFolder;
		
		workResource = getRoot().getChild(relativeFolderPath);
		
		if (workResource.isFolder()) {
			contextFolder = (IManagedFolder)workResource;
		} else {
			contextFolder = workResource.getParent();
		}
		
		if (!contextFolder.isCVSFolder()) {
			throw new CVSException(Policy.bind("Command.invalidResource", new Object[] {contextFolder.toString()}));
		}
		
		return workResource;
	}
	
	/**
	 * If mResource is a folder:<br>
	 * Send all Directory under mResource as arguments to the server<br>
	 * If mResource is a file:<br>
	 * Send the file to the server<br>
	 * <br>
	 * Files that are changed are send with the content.
	 * 
	 * @param modifiedOnly sends files that are modified only to the server
	 * @param emptyFolders sends the folder-entrie even if there is no file 
	 		  to send in it
	 */
	protected void sendFileStructure(IManagedResource mResource, 
									IProgressMonitor monitor,
									boolean modifiedOnly,
									boolean emptyFolders) throws CVSException {
		
		FileStructureVisitor fsVisitor;
		
		fsVisitor = new FileStructureVisitor(requestSender,mRoot,monitor,modifiedOnly,emptyFolders);
		
		// FIXME: The accept should have an IProgressMonitor argment, not the above constructor
		mResource.accept(fsVisitor);

	}
	
	/**
	 * Send an array of Resources.
	 * 
	 * @see Command#sendFileStructure(IManagedResource,IProgressMonitor,boolean,boolean,boolean)
	 */
	protected void sendFileStructure(IManagedResource[] mResources, 
									IProgressMonitor monitor,
									boolean modifiedOnly,
									boolean emptyFolders) throws CVSException {
		
		for (int i=0; i<mResources.length; i++) {
			sendFileStructure(mResources[i],
								monitor,
								modifiedOnly,
								emptyFolders);
		}
	}					

	/**
	 * Checks that all the workResources are managed Resources.
	 * (For folders we check isCVSFolder, because of a project-folder
	 * that is not managed, because it is not registerd in the 
	 * parent-folder<br>
	 * To be used this way: Assert.isTrue(allArgumentsManaged())
	 * 
	 * @throws AssertionFailedException if not all the arguments are
	 *          managed
	 */
	protected boolean allResourcesManaged() throws RuntimeException {

		IManagedResource[] mWorkResources;		

		try {
			mWorkResources = getWorkResources();
		
			for (int i=0; i<mWorkResources.length; i++) {
				if (mWorkResources[i].isFolder()) {
					Assert.isTrue(((IManagedFolder) mWorkResources[i]).isCVSFolder());
				} else {
					Assert.isTrue(mWorkResources[i].isManaged());
				}	
			}
		} catch (CVSException e) {
			Assert.isTrue(false);
		}
					  		
		return true;
	}
	
}

