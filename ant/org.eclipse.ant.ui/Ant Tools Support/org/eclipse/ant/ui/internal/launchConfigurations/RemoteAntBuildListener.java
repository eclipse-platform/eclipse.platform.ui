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

package org.eclipse.ant.ui.internal.launchConfigurations;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.ant.ui.internal.model.AntUtil;
import org.eclipse.ant.ui.internal.model.IAntUIConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.text.Region;

/**
 * Parts adapted from org.eclipse.jdt.internal.junit.ui.RemoteTestRunnerClient
 * The client side of the RemoteAntBuildLogger. Handles the
 * marshalling of the different messages.
 */
public class RemoteAntBuildListener {
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
	private PrintWriter fWriter;
	private BufferedReader fBufferedReader;
	private boolean fDebug= false;
	private IProcess fProcess;
	private String fProcessId;
	private File fBuildFileParent= null;
	
	/**
	 * Reads the message stream from the RemoteTestRunner
	 */
	private class ServerConnection extends Thread {
		private int fPort;
		
		public ServerConnection(int port) {
			super("ServerConnection"); //$NON-NLS-1$
			fPort= port;
		}
		
		public void run() {
			try {
				if (fDebug) {
					System.out.println("Creating server socket " + fPort); //$NON-NLS-1$
				}
				fServerSocket= new ServerSocket(fPort);
				fSocket= fServerSocket.accept();				
				fBufferedReader= new BufferedReader(new InputStreamReader(fSocket.getInputStream()));
				fWriter= new PrintWriter(fSocket.getOutputStream(), true);
				String message;
				while(fBufferedReader != null && (message= fBufferedReader.readLine()) != null) {
					receiveMessage(message);
				}
			} catch (SocketException e) {
				//notifyTestRunTerminated();
			} catch (IOException e) {
				// fall through
			}
			shutDown();
		}
	}

	/**
	 * Start listening to a test run. Start a server connection that
	 * the RemoteAntBuildLogger can connect to.
	 */
	public synchronized void startListening(int port) {
		fPort = port;
		ServerConnection connection = new ServerConnection(port);
		connection.start();
	}
	
	/**
	 * Requests to stop the remote Ant build.
	 */
	public synchronized void cancelBuild() {
		if (isRunning()) {
			fWriter.println(MessageIds.BUILD_CANCELLED);
			fWriter.flush();
		}
	}

	private synchronized void shutDown() {
		if (fDebug) {
			System.out.println("shutdown " + fPort); //$NON-NLS-1$
		}
		if (fWriter != null) {
			fWriter.close();
			fWriter= null;
		}
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
	
	public boolean isRunning() {
		return fSocket != null;
	}
		
	private void receiveMessage(String message) {
		if (message.startsWith(MessageIds.TASK)) {
			message= message.substring(MessageIds.TASK.length());
			
			int index= message.indexOf(',');
			String name= message.substring(0, index);
			int index2= message.indexOf(',', index + 1);
			int lineLength= Integer.parseInt(message.substring(index + 1, index2));
			int finalIndex= index2 + 1 + lineLength;
			String line= message.substring(index2 + 1, finalIndex);
			String location= message.substring(finalIndex + 1);
	
			int size = IAntUIConstants.LEFT_COLUMN_SIZE - (name.length() + 3);
			int offset = Math.max(size - 2, 1);
			int length = IAntUIConstants.LEFT_COLUMN_SIZE - size - 3;
			IConsoleHyperlink taskLink = AntUtil.getTaskLink(location, fBuildFileParent);
			if (taskLink != null) {
				TaskLinkManager.addTaskHyperlink(getProcess(), taskLink, new Region(offset, length), line);
			}
		} else if (message.startsWith(MessageIds.PROCESS_ID)) {
			message= message.substring(MessageIds.PROCESS_ID.length());
			fProcessId= message;
		} else if (message.startsWith("Buildfile:")) { //$NON-NLS-1$
			String fileName = message.substring(10).trim();
			IFile file = AntUtil.getFileForLocation(fileName, fBuildFileParent);
			if (file != null) {
				FileLink link = new FileLink(file, null,  -1, -1, -1);
				TaskLinkManager.addTaskHyperlink(getProcess(), link, new Region(11 + System.getProperty("line.separator").length(), fileName.length()), fileName); //$NON-NLS-1$
				fBuildFileParent= file.getLocation().toFile().getParentFile();
			}
		}
	}
	
	/**
	 * Returns the associated process, finding it if necessary, if not
	 * already found.
	 */
	private IProcess getProcess() {
		if (fProcess == null) {
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
}
