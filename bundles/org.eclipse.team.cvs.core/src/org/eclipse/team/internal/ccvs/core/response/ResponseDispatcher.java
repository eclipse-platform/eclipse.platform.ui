package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;

/**
 * The ResponseContainer manages the respones of the server and
 * pipes them to the appropiate handlers.
 * 
 * It also takes care about registering handlers for response-tokens
 * form the server. Standard-handlers are loaded on creation.
 */
public class ResponseDispatcher {
	
	public static final String OK = "ok";	
	public static final String ERROR = "error";
	
	private Hashtable standardResponsePool;
	private Hashtable replaceResponsePool;
	private Connection connection;
	// Idea: private IResponse[] addResponsePool;
	
	/**
	 * Puts all the Request in a container in order to have access
	 * to them when they come from the stream
	 * 
	 * Generic approach to "plug in" new responses just by adding them
	 * to this constructor
	 */	
	public ResponseDispatcher(Connection connection, IResponseHandler[] customHandlers) {
		
		ModTimeHandler modTimeHandler = new ModTimeHandler();
		
		this.connection = connection;
		
		standardResponsePool = new Hashtable();
		replaceResponsePool = new Hashtable();
		
		registerStandardHandler(modTimeHandler);
		registerStandardHandler(new CopyHandler());
		registerStandardHandler(new RemoveEntry());
		registerStandardHandler(new Updated(modTimeHandler,true));
		registerStandardHandler(new Updated(modTimeHandler,false));
		// registerStandardHandler(new UpdateExisting(modTimeHandler));
		registerStandardHandler(new UnsupportedHandler("Valid-requests"));
		registerStandardHandler(new CheckedIn());
		registerStandardHandler(new Removed());
		registerStandardHandler(new MessageOutputHandler("M"));
		registerStandardHandler(new MessageOutputHandler("E"));
		registerStandardHandler(new StaticHandler(true));
		registerStandardHandler(new StaticHandler(false));
		registerStandardHandler(new StickyHandler(true));
		registerStandardHandler(new StickyHandler(false));

		if (customHandlers != null) {
			for (int i=0;i<customHandlers.length;i++) {
				registerResponseHandler(customHandlers[i]);
			}
		}
		
	}
	
	/**
	 * Get the handler matching the string. Take it from the replaceResponsePool if 
	 * possible, otherwise take it from the standardResponsePool.
	 * 
	 * If there is no matching handler at all, return a standard-handler
	 */
	private IResponseHandler getHandler(String responseToken) {
		
		IResponseHandler responseHandler = (IResponseHandler) replaceResponsePool.get(responseToken);
		
		if (responseHandler == null) {
			responseHandler = (IResponseHandler) standardResponsePool.get(responseToken);
		}
		
		if (responseHandler == null) {
			responseHandler = new DefaultHandler();
		}
		
		return responseHandler;
	}
	
	/**
	 * Give a list of all registered Responses from the Server.
	 * 
	 * (ok, error is added, because they are not fromal 
	 * registerd as handler)
	 * 
	 */
	public String makeResponseList() {
		
		StringBuffer result = new StringBuffer("ok error");		
		
		/* We are only looking into the standardResponsePool
		   all the registerd responses must be here as well,
		   otherwise you are not allowed to register special 
		   handler
		*/
		Iterator elements = standardResponsePool.values().iterator();
		while (elements.hasNext()) {
			IResponseHandler handler = (IResponseHandler) elements.next();
			result.append(' ');
			result.append(handler.getName());
		}
		
		return result.toString();
	}
	
	/**
	 * Given a token of response from the server, this method
	 * reacts on it and does the appropiate handling with the
	 * responseHandler, that are loaded in it.
	 */
	public void handle(String responseToken,
							Connection connection, 
							PrintStream messageOutput,
							ICVSFolder mRoot,
							IProgressMonitor monitor) 
							throws CVSException {
		
		IResponseHandler responseHandler;
		
		responseHandler = getHandler(responseToken);
		responseHandler.handle(connection, messageOutput, mRoot, monitor);
		
	}
	
	/**
	 * To register a non-standard responseHandler.
	 * 
	 * Replaces the preloaded responshandler for one response of the 
	 * server. The name of the replaced response is response.getName().
	 * 
	 * If the response is not known to the server, then the call crashes.
	 * 
	 */
	public void registerResponseHandler(IResponseHandler responseHandler) {
		
		Assert.isNotNull(standardResponsePool.get(responseHandler.getName()));
		Assert.isTrue(replaceResponsePool.get(responseHandler.getName()) == null);

		replaceResponsePool.put(responseHandler.getName(),responseHandler);
	}
	
	/**
	 * To unregister a non-standard responseHandler.
	 * 
	 */
	public void unregisterResponseHandler(IResponseHandler responseHandler) {
		
		Assert.isNotNull(standardResponsePool.get(responseHandler.getName()));
		Assert.isNotNull(replaceResponsePool.get(responseHandler.getName()));
		
		replaceResponsePool.remove(responseHandler.getName());
	}	
	
	/**
	 * Registers a standard-handler while doing the 
	 * init.
	 */
	private void registerStandardHandler(IResponseHandler responseHandler) {
		
		Assert.isTrue(standardResponsePool.get(responseHandler.getName()) == null);
		
		standardResponsePool.put(responseHandler.getName(),responseHandler);
	}

	/**
	 * Runs the response event loop.
	 * 
	 * If OK is in the pipe       => stop looping.
	 * In Error is in the pipe    => throw error, stop looping.
	 * Something else in the pipe => handle it with handle(response) 
	 * 									and continou looping
	 * 
	 * Does the work with the monitor
	 */	
	public void manageResponse(IProgressMonitor monitor, 
								ICVSFolder mRoot,
								PrintStream messageOutput) 
								throws CVSException {

		// This number can be tweaked if the monitor is judged to move too
		// quickly or too slowly. After some experimentation this is a good
		// number for both large projects (it doesn't move so quickly as to
		// give a false sense of speed) and smaller projects (it actually does
		// move some rather than remaining still and then jumping to 100).
		final int TOTAL_WORK = 300;
		monitor.beginTask(Policy.bind("ResponseDispatcher.receiving"), TOTAL_WORK);
		
		int halfWay = TOTAL_WORK / 2;
		int currentIncrement = 4;
		int nextProgress = currentIncrement;
		int worked = 0;

		connection.flush();
				
		try {
			while (true) {
				String response = connection.readToken();
				
				// Update monitor work amount
				if (--nextProgress <= 0) {
					monitor.worked(1);
					worked++;
					if (worked >= halfWay) {
						//we have passed the current halfway point, so double the
						//increment and reset the halfway point.
						currentIncrement *= 2;
						halfWay += (TOTAL_WORK - halfWay) / 2;				
					}
					//reset the progress counter to another full increment
					nextProgress = currentIncrement;
				}			
				Policy.checkCanceled(monitor);
				
				// Distiguage between three different tokens:
				//   OK    => break
				//   ERROR => throw error (implicit break)
				//   rest  => handle it
				if (response.startsWith(OK)) {
					checkForErrors(connection);
					break;
				} else if (ERROR.equals(response)) {
					throw generateServerException(connection);
				} else {
					handle(response,connection,messageOutput,mRoot,monitor);
				}
			}
		} finally {
			monitor.done();
		}
	}
	
	/*
	 * Create the CVSServerException for the given connection
	 */
	private CVSServerException generateServerException(Connection connection) {
		
		String message = ERROR;
		
		// The error tag can be followed by an error message too
		if (connection.getLastUsedDelimiterToken() == IResponseHandler.BLANK_DELIMITER) {
			try {
				message = connection.readLine();
			} catch (CVSException e) {
				// We get nothing and go on sending the standard-message
			}
		}
		
		if (message.equals("") || message.equals(" ")) {
			message = Policy.bind("ResponseDispatcher.serverError");
		}

		CVSServerException e = CVSServerException.forError(message, connection.getErrors());
		connection.resetErrors();
		return e;
	}
	
	/*
	 * Check to see if any handlers registered errors even if the sevrer didn't report one.
	 * 
	 * If there are, throw an exception
	 */
	 private void checkForErrors(Connection connection) throws CVSException {
	 	if (connection.hasErrors()) {
	 		CVSServerException e = CVSServerException.forError(Policy.bind("ResponseDispatcher.problemsReported"), connection.getErrors());
			connection.resetErrors();
			throw e;
	 	}
	 }
}

