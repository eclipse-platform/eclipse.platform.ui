package org.eclipse.ant.ui.internal.antsupport.logger;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Location;

/**
 * Parts adapted from org.eclipse.jdt.internal.junit.runner.RemoteTestRunner
 * A build logger that reports via a socket connection.
 * See MessageIds for more information about the protocol.
 */
public class RemoteAntBuildLogger extends DefaultLogger {

	/**
	 * The client socket.
	 */
	private Socket fClientSocket;
	/**
	 * Print writer for sending messages
	 */
	private PrintWriter fWriter;
	/**
	 * Reader for incoming messages
	 */
	private BufferedReader fReader;
	/**
	 * Host to connect to, default is the localhost
	 */
	private String fHost= ""; //$NON-NLS-1$
	/**
	 * Port to connect to.
	 */
	private int fPort= -1;
	/**
	 * Is the debug mode enabled?
	 */
	private boolean fDebugMode= false;	
	
	private boolean fCancelled;
	
	private boolean fSentProcessId= false;
	
	private List fEventQueue;
	
	/**
	 * Thread reading from the socket
	 */
	private ReaderThread fReaderThread;
	/**
	 * Reader thread that processes messages from the client.
	 */
	private class ReaderThread extends Thread {

		public ReaderThread() {
			super("ReaderThread"); //$NON-NLS-1$
		}

		public void run(){
			try { 
				String message= null; 
				while (true) { 
					if ((message= fReader.readLine()) != null) {
						
						if (message.startsWith(MessageIds.BUILD_CANCELLED)){
							fCancelled= true;
							//RemoteAntBuildLogger.this.stop();
							synchronized(RemoteAntBuildLogger.this) {
								RemoteAntBuildLogger.this.notifyAll();
							}
							break;
						}
					}
				} 
			} catch (Exception e) {
				//RemoteTestRunner.this.stop();
			}
		}
	}
	
	/**
	 * The class loader to be used for loading tests.
	 * Subclasses may override to use another class loader.
	 */
	protected ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}
	
	/**
	 * Connect to the remote test listener.
	 */
	private void connect() {
		if (fDebugMode) {
			System.out.println("RemoteAntBuildLogger: trying to connect" + fHost + ":" + fPort); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		for (int i= 1; i < 20; i++) {
			try{
				fClientSocket= new Socket(fHost, fPort);
				fWriter= new PrintWriter(fClientSocket.getOutputStream(), false/*true*/);
				fReader= new BufferedReader(new InputStreamReader(fClientSocket.getInputStream()));
				fReaderThread= new ReaderThread();
				fReaderThread.start();
				return;
			} catch(IOException e){
			}
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {
			}
		}
		shutDown();
	}

	/**
	 * Shutdown the connection to the remote build listener.
	 */
	private void shutDown() {
		if (fEventQueue != null) {
			fEventQueue.clear();
		}
		if (fWriter != null) {
			fWriter.close();
			fWriter= null;
		}
		try {
			if (fReaderThread != null)   {
				// interrupt reader thread so that we don't block on close
				// on a lock held by the BufferedReader
				// fix for bug: 38955
				fReaderThread.interrupt();
			}
			if (fReader != null) {
				fReader.close();
				fReader= null;
			}
		} catch(IOException e) {
		}
		
		try {
			if (fClientSocket != null) {
				fClientSocket.close();
				fClientSocket= null;
			}
		} catch(IOException e) {
		}
	}

	private void sendMessage(String msg) {
		if(fWriter == null) {
			return;
		}
		fWriter.println(msg);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
		super.buildFinished(event);
		shutDown();
	}

	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
		super.targetStarted(event);
		if (!fSentProcessId) {
			String portProperty= event.getProject().getProperty("eclipse.connect.port"); //$NON-NLS-1$
			if (portProperty != null) {
				fPort= Integer.parseInt(portProperty);
				connect();
			}
			
			fSentProcessId= true;
			StringBuffer message= new StringBuffer(MessageIds.PROCESS_ID);
			message.append(event.getProject().getProperty("org.eclipse.ant.ui.ATTR_ANT_PROCESS_ID")); //$NON-NLS-1$
			sendMessage(message.toString());
			if (fEventQueue != null) {
				for (Iterator iter = fEventQueue.iterator(); iter.hasNext();) {
					processEvent((BuildEvent)iter.next());
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#messageLogged(org.apache.tools.ant.BuildEvent)
	 */
	public void messageLogged(BuildEvent event) {
		if (fCancelled) {
			shutDown();
			System.exit(0);
		}
		int priority = event.getPriority();
		
		if (priority > msgOutputLevel) {
			return;
		}
		super.messageLogged(event);
		
		if (!fSentProcessId) {
			if (fEventQueue == null){
				fEventQueue= new ArrayList(10);
			}
			fEventQueue.add(event);
			return;
		}
		processEvent(event);
	}

	private void processEvent(BuildEvent event) {
		if (event.getTask() != null & !emacsMode) {
			try {
				BufferedReader r = new BufferedReader(new StringReader(event.getMessage()));
				String line = r.readLine();
				StringBuffer message= null;
				String taskName= event.getTask().getTaskName();
				StringBuffer labelBuff= new StringBuffer();
				labelBuff.append('[');
				labelBuff.append(taskName);
				labelBuff.append("] "); //$NON-NLS-1$
				String label= labelBuff.toString();
				Location location= event.getTask().getLocation();
				while (line != null) {
					message= new StringBuffer(MessageIds.TASK);
					message.append(taskName);
					message.append(',');
					line= (label + line).trim();
					message.append(line.length());
					message.append(',');
					message.append(line);
					message.append(',');
					message.append(location);
					sendMessage(message.toString());
					line = r.readLine();
				}
			} catch (IOException e) {
			}
		}
	}
}