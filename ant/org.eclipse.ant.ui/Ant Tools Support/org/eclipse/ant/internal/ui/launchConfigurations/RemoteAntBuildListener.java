/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.launchConfigurations;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.Project;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.console.IHyperlink;

/**
 * Parts adapted from org.eclipse.jdt.internal.junit.ui.RemoteTestRunnerClient
 * The client side of the RemoteAntBuildLogger. Handles the
 * marshalling of the different messages.
 */
public class RemoteAntBuildListener implements ILaunchesListener {
	public abstract class ListenerSafeRunnable implements ISafeRunnable {
		public void handleException(Throwable exception) {
			AntUIPlugin.log(exception);
		}
	}

	/**
	 * The server socket
	 */
	private ServerSocket fServerSocket;
	private Socket fSocket;
	private int fPort= -1;
	private BufferedReader fBufferedReader;
	private boolean fDebug= false;
	private IProcess fProcess;
	private String fProcessId;
	private File fBuildFileParent= null;
	private List fMessageQueue;
	protected ILaunch fLaunch;
	
	/**
	 * Reads the message stream from the RemoteAntBuildLogger
	 */
	private class ServerConnection extends Thread {
		private int fServerPort;
		
		public ServerConnection(int port) {
			super("Ant Build Server Connection"); //$NON-NLS-1$
			setDaemon(true);
			fServerPort= port;
		}
		
		public void run() {
			try {
				if (fDebug) {
					System.out.println("Creating server socket " + fServerPort); //$NON-NLS-1$
				}
				fServerSocket= new ServerSocket(fServerPort);
				fSocket= fServerSocket.accept();			
				if (fDebug) {
					System.out.println("Connection"); //$NON-NLS-1$
				}	
				fBufferedReader= new BufferedReader(new InputStreamReader(fSocket.getInputStream()));
				String message;
				while(fBufferedReader != null && (message= fBufferedReader.readLine()) != null) {
					receiveMessage(message);
				}
			} catch (SocketException e) {
			} catch (IOException e) {
				// fall through
			}
			shutDown();
		}
	}
	
	public RemoteAntBuildListener(ILaunch launch) {
		super();
		fLaunch= launch;
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
	}

	/**
	 * Start listening to an Ant build. Start a server connection that
	 * the RemoteAntBuildLogger can connect to.
	 * 
	 * @param port The port number to create the server connection on
	 */
	public synchronized void startListening(int eventPort) {
		fPort = eventPort;
		ServerConnection connection = new ServerConnection(eventPort);
		connection.start();
	}

	protected synchronized void shutDown() {
		if (fDebug) {
			System.out.println("shutdown " + fPort); //$NON-NLS-1$
		}
		fLaunch= null;
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		try {
			if (fBufferedReader != null) {
				fBufferedReader.close();
				fBufferedReader= null;
			}
		} catch(IOException e) {
		}	
		try{
			if(fSocket != null) {
				fSocket.close();
				fSocket= null;
			}
		} catch(IOException e) {
		}
		try{
			if(fServerSocket != null) {
				fServerSocket.close();
				fServerSocket= null;
			}
		} catch(IOException e) {
		}
	}
		
	protected void receiveMessage(String message) {
		if (fDebug) {
			System.out.println(message);
		}
		if (message.startsWith(MessageIds.TASK)) {
			message= message.substring(MessageIds.TASK.length());
			
			int index= message.indexOf(',');
			int priority= Integer.parseInt(message.substring(0, index));
			int index2= message.indexOf(',', index + 1);
			String taskName= message.substring(index + 1, index2);
			int index3= message.indexOf(',', index2 + 1);
			int lineLength= Integer.parseInt(message.substring(index2 + 1, index3));
			int finalIndex= index3 + 1 + lineLength;
			String line= message.substring(index3 + 1, finalIndex);
			String location= message.substring(finalIndex + 1);
	
			int size = IAntUIConstants.LEFT_COLUMN_SIZE - (taskName.length() + 3);
			int offset = Math.max(size - 2, 1);
			int length = IAntUIConstants.LEFT_COLUMN_SIZE - size - 3;
			IHyperlink taskLink = AntUtil.getTaskLink(location, fBuildFileParent);
			if (taskLink != null) {
				TaskLinkManager.addTaskHyperlink(getProcess(), taskLink, new Region(offset, length), line);
			}
			
			StringBuffer fullMessage= new StringBuffer();
			adornMessage(taskName, line, fullMessage);
			writeMessage(fullMessage.append(System.getProperty("line.separator")).toString(), priority); //$NON-NLS-1$
		} else if (message.startsWith(MessageIds.PROCESS_ID)) {
			message= message.substring(MessageIds.PROCESS_ID.length());
			fProcessId= message;
		} else {
			int index= message.indexOf(',');
			if (index > 0) {
				int priority= Integer.parseInt(message.substring(0, index));
				message= message.substring(index + 1);
				writeMessage(message + System.getProperty("line.separator"), priority); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Returns the associated process, finding it if necessary.
	 */
	protected IProcess getProcess() {
		if (fProcess == null && fProcessId != null) {
			IProcess[] all = DebugPlugin.getDefault().getLaunchManager().getProcesses();
			for (int i = 0; i < all.length; i++) {
				IProcess process = all[i];
				if (fProcessId.equals(process.getAttribute(AntProcess.ATTR_ANT_PROCESS_ID))) {
					fProcess = process;
					break;
				}
			}
		}
		return fProcess;
	}
	
	private AntStreamMonitor getMonitor(int priority) {
		IProcess process= getProcess();
		if (process == null) {
			return null;
		}
		AntStreamsProxy proxy = (AntStreamsProxy)process.getStreamsProxy();
		if (proxy == null) {
			return null;
		}
		AntStreamMonitor monitor = null;
		switch (priority) {
			case Project.MSG_INFO:
				monitor = (AntStreamMonitor)proxy.getOutputStreamMonitor();
				break;
			case Project.MSG_ERR:
				monitor = (AntStreamMonitor)proxy.getErrorStreamMonitor();
				break;
			case Project.MSG_DEBUG:
				monitor = (AntStreamMonitor)proxy.getDebugStreamMonitor();
				break;
			case Project.MSG_WARN:
				monitor = (AntStreamMonitor)proxy.getWarningStreamMonitor();
				break;
			case Project.MSG_VERBOSE:
				monitor = (AntStreamMonitor)proxy.getVerboseStreamMonitor();
				break;
		}
		return monitor;
	}
	
	/**
	 * Builds a right justified task prefix for the given build event, placing it
	 * in the given string buffer.
	 *  
	 * @param event build event
	 * @param fullMessage buffer to place task prefix in
	 */
	private void adornMessage(String taskName, String line, StringBuffer fullMessage) {
		if (taskName == null) {
			taskName = "null"; //$NON-NLS-1$
		}
		
		int size = IAntUIConstants.LEFT_COLUMN_SIZE - (taskName.length() + 6);
		for (int i = 0; i < size; i++) {
			fullMessage.append(' ');
		}
		
		fullMessage.append(line);
	}
	
	protected void writeMessage(String message, int priority) {
		AntStreamMonitor monitor= getMonitor(priority);
		if (monitor == null) {
			if (fMessageQueue == null) {
				fMessageQueue= new ArrayList();
			}
			fMessageQueue.add(message);
			return;
		}
		if (fMessageQueue != null) {
			for (Iterator iter = fMessageQueue.iterator(); iter.hasNext();) {
				String oldMessage = (String) iter.next();
				monitor.append(oldMessage);
			}
			fMessageQueue= null;
		}
		monitor.append(message);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {
		for (int i = 0; i < launches.length; i++) {
			ILaunch launch = launches[i];
			if (launch.equals(fLaunch)) {
				shutDown();
				return;
			}
		}
	}
}
