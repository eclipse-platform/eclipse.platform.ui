/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.IConsoleListener;

/**
 * Abstract base class for requests that are to be sent to the server.
 */
public abstract class Request {
	public static final ExpandModules EXPAND_MODULES = new ExpandModules();
	public static final ValidRequests VALID_REQUESTS = new ValidRequests();

	/*** Response handler map ***/
	private static final Map responseHandlers = new HashMap();
	static {
		registerResponseHandler(new CheckedInHandler());
		registerResponseHandler(new CopyHandler());
		registerResponseHandler(new ModTimeHandler());
		registerResponseHandler(new NewEntryHandler());
		registerResponseHandler(new RemovedHandler());
		registerResponseHandler(new RemoveEntryHandler());
		registerResponseHandler(new StaticHandler(true));
		registerResponseHandler(new StaticHandler(false));
		registerResponseHandler(new StickyHandler(true));
		registerResponseHandler(new StickyHandler(false));
		registerResponseHandler(new UpdatedHandler(UpdatedHandler.HANDLE_UPDATED));
		registerResponseHandler(new UpdatedHandler(UpdatedHandler.HANDLE_UPDATE_EXISTING));
		registerResponseHandler(new UpdatedHandler(UpdatedHandler.HANDLE_CREATED));
		registerResponseHandler(new UpdatedHandler(UpdatedHandler.HANDLE_MERGED));
		registerResponseHandler(new ValidRequestsHandler());
		registerResponseHandler(new ModuleExpansionHandler());
		registerResponseHandler(new MTHandler());
	}
	protected static void registerResponseHandler(ResponseHandler handler) {
		responseHandlers.put(handler.getResponseID(), handler);
	}
	protected static void removeResponseHandler(String responseID) {
		responseHandlers.remove(responseID);
	}
	protected static ResponseHandler getResponseHandler(String responseID) {
		return (ResponseHandler)responseHandlers.get(responseID);
	}

	/**
	 * Prevents client code from instantiating us.
	 */
	protected Request() { }

	/**
	 * Returns the string used to invoke this request on the server.
	 * [template method]
	 * 
	 * @return the request identifier string
	 */
	protected abstract String getRequestId();

	/**
	 * Executes a request and processes the responses.
	 * 
	 * @param session the open CVS session
	 * @param listener the command output listener, or null to discard all messages
	 * @param monitor the progress monitor
	 * @return a status code indicating success or failure of the operation
	 */
	protected IStatus executeRequest(Session session, ICommandOutputListener listener,
		IProgressMonitor monitor) throws CVSException {
		// send request
		session.sendRequest(getRequestId());

		// This number can be tweaked if the monitor is judged to move too
		// quickly or too slowly. After some experimentation this is a good
		// number for both large projects (it doesn't move so quickly as to
		// give a false sense of speed) and smaller projects (it actually does
		// move some rather than remaining still and then jumping to 100).
		final int TOTAL_WORK = 300;
		monitor.beginTask(Policy.bind("Command.receivingResponses"), TOTAL_WORK); //$NON-NLS-1$
		int halfWay = TOTAL_WORK / 2;
		int currentIncrement = 4;
		int nextProgress = currentIncrement;
		int worked = 0;
		
		// If the session is connected to a CVSNT server (1.11.1.1), we'll need to do some special handling for
		// some errors. Unfortunately, CVSNT 1.11.1.1 will drop the connection after so some functionality is
		// still effected
		boolean isCVSNT = session.isCVSNT();

		List accumulatedStatus = new ArrayList();
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
			} else argument = "";  //$NON-NLS-1$

			// handle completion responses
			if (response.equals("ok")) {  //$NON-NLS-1$
				break;
			} else if (response.equals("error") || (isCVSNT && response.equals(""))) {  //$NON-NLS-1$ //$NON-NLS-2$
				if (argument.trim().length() == 0) {
					argument = Policy.bind("Command.serverError", Policy.bind("Command." + getRequestId()));  //$NON-NLS-1$  //$NON-NLS-2$
				}
				if (accumulatedStatus.isEmpty()) {
					accumulatedStatus.add(new CVSStatus(CVSStatus.ERROR, CVSStatus.SERVER_ERROR, Policy.bind("Command.noMoreInfoAvailable")));//$NON-NLS-1$
				}
				return new MultiStatus(CVSProviderPlugin.ID, CVSStatus.SERVER_ERROR, 
					(IStatus[]) accumulatedStatus.toArray(new IStatus[accumulatedStatus.size()]),
					argument, null);
			// handle message responses
			} else if (response.equals("MT")) {  //$NON-NLS-1$
				// Handle the MT response
				MTHandler handler = (MTHandler) responseHandlers.get(response);
				if (handler != null) {
					handler.handle(session, argument, monitor);
				} else {
					throw new CVSException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
						CVSProviderPlugin.ID, CVSException.IO_FAILED,
						Policy.bind("Command.unsupportedResponse", response, argument), null)); //$NON-NLS-1$
				}
				// If a line is available, pass it on to the message listener 
				// and console as if it were an M response
				if (handler.isLineAvailable()) {
					String line = handler.getLine();
					IStatus status = listener.messageLine(line, session.getLocalRoot(), monitor);
					if (status != ICommandOutputListener.OK) accumulatedStatus.add(status);
					if (session.isOutputToConsole()) {
						IConsoleListener consoleListener = CVSProviderPlugin.getPlugin().getConsoleListener();
						if (consoleListener != null) consoleListener.messageLineReceived(line);
					}
				}
			} else if (response.equals("M")) {  //$NON-NLS-1$
				IStatus status = listener.messageLine(argument, session.getLocalRoot(), monitor);
				if (status != ICommandOutputListener.OK) accumulatedStatus.add(status);
				if (session.isOutputToConsole()) {
					IConsoleListener consoleListener = CVSProviderPlugin.getPlugin().getConsoleListener();
					if (consoleListener != null) consoleListener.messageLineReceived(argument);
				}
			} else if (response.equals("E")) { //$NON-NLS-1$
				IStatus status = listener.errorLine(argument, session.getLocalRoot(), monitor);
				if (status != ICommandOutputListener.OK) accumulatedStatus.add(status);
				if (session.isOutputToConsole()) {
					IConsoleListener consoleListener = CVSProviderPlugin.getPlugin().getConsoleListener();
					if (consoleListener != null) consoleListener.errorLineReceived(argument);
				}
			// handle other responses
			} else {
				ResponseHandler handler = (ResponseHandler) responseHandlers.get(response);
				if (handler != null) {
					handler.handle(session, argument, monitor);
				} else {
					throw new CVSException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
						CVSProviderPlugin.ID, CVSException.IO_FAILED,
						Policy.bind("Command.unsupportedResponse", response, argument), null)); //$NON-NLS-1$
				}
			}
		}
		if (accumulatedStatus.isEmpty()) {
			return ICommandOutputListener.OK;
		} else {
			return new MultiStatus(CVSProviderPlugin.ID, CVSStatus.INFO,
				(IStatus[]) accumulatedStatus.toArray(new IStatus[accumulatedStatus.size()]),
				Policy.bind("Command.warnings", Policy.bind("Command." + getRequestId())), null);  //$NON-NLS-1$  //$NON-NLS-2$
		}
	}
	
	/**
	 * Makes a list of all valid responses; for initializing a session.
	 * @return a space-delimited list of all valid response strings
	 */
	static String makeResponseList() {
		StringBuffer result = new StringBuffer("ok error M E");  //$NON-NLS-1$
		Iterator elements = responseHandlers.keySet().iterator();
		while (elements.hasNext()) {
			result.append(' ');
			result.append((String) elements.next());
		}
		
		return result.toString();
	}
}
