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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.eclipse.ant.ui.internal.antsupport.AntSecurityException;
import org.eclipse.ant.ui.internal.antsupport.InternalAntMessages;

/**
 * Parts adapted from org.eclipse.jdt.internal.junit.runner.RemoteTestRunner
 * A build logger that reports via a socket connection.
 * See MessageIds for more information about the protocol.
 */
public class RemoteAntBuildLogger extends DefaultLogger {

	/** Time of the start of the build */
    private long startTime = System.currentTimeMillis();

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
	
	private List fMessageQueue;
	
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
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.DefaultLogger#printMessage(java.lang.String, java.io.PrintStream, int)
	 */
	protected void printMessage(String message, PrintStream stream, int priority) {
		super.printMessage(message, stream, priority);
		StringBuffer sendMessage= new StringBuffer();
		sendMessage.append(priority);
		sendMessage.append(',');
		sendMessage.append(message.trim());
		sendMessage(sendMessage.toString());
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
		if (fWriter == null) {
			if (msg != null) {
				if (fMessageQueue == null) {
					fMessageQueue= new ArrayList();
				}
				fMessageQueue.add(msg);
			}
			return;
		}
		
		if (fMessageQueue != null) {
			for (Iterator iter = fMessageQueue.iterator(); iter.hasNext();) {
				String message = (String) iter.next();
				fWriter.println(message);
			}
			fMessageQueue= null;
		}
		fWriter.println(msg);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
		handleException(event);
        printMessage( getTimeString(System.currentTimeMillis() - startTime), out, Project.MSG_INFO); 
		shutDown();
	}
	
	protected void handleException(BuildEvent event) {
		Throwable exception = event.getException();
		if (exception == null
		|| exception instanceof AntSecurityException) {
			return;
		}
		printMessage(MessageFormat.format(InternalAntMessages.getString("RemoteAntBuildLogger.BUILD_FAILED__{0}_1"), new String[] { exception.toString()}), //$NON-NLS-1$
					out, Project.MSG_ERR);	
	}
	
	private String getTimeString(long milliseconds) {
		long seconds = milliseconds / 1000;
		long minutes = seconds / 60;
		seconds= seconds % 60;

		StringBuffer result= new StringBuffer(InternalAntMessages.getString("RemoteAntBuildLogger.Total_time")); //$NON-NLS-1$
		if (minutes > 0) {
			result.append(minutes);
			if (minutes > 1) {
				result.append(InternalAntMessages.getString("RemoteAntBuildLogger._minutes_2")); //$NON-NLS-1$
			} else {
				result.append(InternalAntMessages.getString("RemoteAntBuildLogger._minute_3")); //$NON-NLS-1$
			}
		}
		if (seconds > 0) {
			if (minutes > 0) {
				result.append(' ');
			}
			result.append(seconds);
	
			if (seconds > 1) {
				result.append(InternalAntMessages.getString("RemoteAntBuildLogger._seconds_4")); //$NON-NLS-1$
			} else {
				result.append(InternalAntMessages.getString("RemoteAntBuildLogger._second_5")); //$NON-NLS-1$
			} 
		}
		if (seconds == 0 && minutes == 0) {
			result.append(milliseconds);
			result.append(InternalAntMessages.getString("RemoteAntBuildLogger._milliseconds_6"));		 //$NON-NLS-1$
		}
		return result.toString();
	}
			

	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
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
				fEventQueue= null;
			}
		}

		if (Project.MSG_INFO <= msgOutputLevel) {
			String msg= event.getTarget().getName() + ":"; //$NON-NLS-1$
			printMessage(msg, out, Project.MSG_INFO);
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
		
		if (event.getPriority() > msgOutputLevel) {
			return;
		}
		
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
				StringBuffer message;
				String taskName= event.getTask().getTaskName();
				StringBuffer labelBuff= new StringBuffer();
				labelBuff.append('[');
				labelBuff.append(taskName);
				labelBuff.append("] "); //$NON-NLS-1$
				String label= labelBuff.toString();
				Location location= event.getTask().getLocation();
				while (line != null) {
					message= new StringBuffer(MessageIds.TASK);
					message.append(event.getPriority());
					message.append(',');
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
		} else {
			StringBuffer sendMessage= new StringBuffer();
			sendMessage.append(event.getPriority());
			sendMessage.append(',');
			sendMessage.append(event.getMessage().trim());
			sendMessage(sendMessage.toString());
		}
	}
}