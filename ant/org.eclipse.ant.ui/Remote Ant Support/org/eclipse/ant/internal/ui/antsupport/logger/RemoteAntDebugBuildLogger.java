/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.antsupport.logger;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 * Parts adapted from org.eclipse.jdt.internal.junit.runner.RemoteTestRunner
 * A build logger that reports via a socket connection.
 * See MessageIds for more information about the protocol.
 */
public class RemoteAntDebugBuildLogger extends RemoteAntBuildLogger {
	
	private ServerSocket fServerSocket;
	private Socket fRequestSocket;
	
	private PrintWriter fRequestWriter;
	
	private BufferedReader fRequestReader;
	
	protected boolean fStepSuspend= false;
	protected boolean fClientSuspend= false;
	
	protected boolean fShouldSuspend= false;
	
	private Task fCurrentTask;
	private Target fCurrentTarget;
	
	private int[] fBreakpoints= null;
	
	private Map fProperties= null;
	
	/**
	 * Request port to connect to.
	 * Used for debug connections
	 */
	protected int fRequestPort= -1;

	/**
	 * Reader thread that processes requests from the debug client.
	 */
	private class ReaderThread extends Thread {
		public ReaderThread() {
			super("ReaderThread"); //$NON-NLS-1$
			setDaemon(true);
		}

		public void run(){
			try { 
				String message= null; 
				while (fRequestReader != null) { 
					if ((message= fRequestReader.readLine()) != null) {
						
						if (message.startsWith(DebugMessageIds.STEP)){
							synchronized(RemoteAntDebugBuildLogger.this) {
								RemoteAntDebugBuildLogger.this.notifyAll();
								fStepSuspend= true;
								sendRequestResponse(DebugMessageIds.RESUMED + DebugMessageIds.STEP);
							}
						} else if (message.startsWith(DebugMessageIds.SUSPEND)) {
							synchronized(RemoteAntDebugBuildLogger.this) {
								fClientSuspend= true;
							}
						} else if (message.startsWith(DebugMessageIds.RESUME)) {
							synchronized(RemoteAntDebugBuildLogger.this) {
								RemoteAntDebugBuildLogger.this.notifyAll();
							}
							sendRequestResponse(DebugMessageIds.RESUMED + DebugMessageIds.CLIENT_REQUEST);
						} else if (message.startsWith(DebugMessageIds.TERMINATE)) {
							shutDown();
							//sendRequestResponse(DebugMessageIds.TERMINATED);
							System.exit(1);
						} else if (message.startsWith(DebugMessageIds.STACK)) {
							if (fCurrentTask == null) {
								//TODO return an error
							}
							marshallStack();
						} else if (message.startsWith(DebugMessageIds.ADD_BREAKPOINT)) {
							addBreakpoint(message);
						} else if (message.startsWith(DebugMessageIds.REMOVE_BREAKPOINT)) {
							removeBreakpoint(message);
						}  else if (message.startsWith(DebugMessageIds.PROPERTIES)) {
							marshallProperties();
						}
					}
				} 
			} catch (Exception e) {
				RemoteAntDebugBuildLogger.this.shutDown();
			}
		}
	}
	
	private void requestConnect() {
		if (fDebugMode) {
			System.out.println("RemoteAntDebugBuildLogger: trying to connect" + fHost + ":" + fRequestPort); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		try{
			fRequestSocket= fServerSocket.accept();
			fRequestWriter= new PrintWriter(fRequestSocket.getOutputStream(), true);
			fRequestReader = new BufferedReader(new InputStreamReader(fRequestSocket.getInputStream()));
			
			ReaderThread readerThread= new ReaderThread();
			readerThread.setDaemon(true);
			readerThread.start();
			return;
		} catch(IOException e){
		}
		
		shutDown();
		//throw new BUildExection()
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.antsupport.logger.RemoteAntBuildLogger#shutDown()
	 */
	protected void shutDown() {
		if (fRequestWriter != null) {
			fRequestWriter.close();
			fRequestWriter= null;
		}
		
		if (fRequestReader != null) {
			try {
				fRequestReader.close();
			} catch (IOException e) {
			}
			fRequestReader= null;
		}
		
		if (fRequestSocket != null) {
			try {
				fRequestSocket.close();	
			} catch(IOException e) {
			}
		}
		fRequestSocket= null;
		
		super.shutDown();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
		super.buildStarted(event);
		marshalMessage(-1, DebugMessageIds.BUILD_STARTED);
		String requestPortProperty= event.getProject().getProperty("eclipse.connect.request_port"); //$NON-NLS-1$
		if (requestPortProperty != null) {
			fRequestPort= Integer.parseInt(requestPortProperty);
			try {
				fServerSocket= new ServerSocket(fRequestPort);
			} catch (IOException ioe) {
				//throw new buildexection();
				shutDown();
			}
			requestConnect();
		} else {
			shutDown();
		}
		fShouldSuspend= true;
		waitIfSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
		marshalMessage(-1, DebugMessageIds.BUILD_FINISHED);
		super.buildFinished(event);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
		super.targetStarted(event);
		fCurrentTarget= event.getTarget();
		marshalMessage(-1, DebugMessageIds.TARGET_STARTED);
		waitIfSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void targetFinished(BuildEvent event) {
		super.targetFinished(event);
		fCurrentTarget= null;
		marshalMessage(-1, DebugMessageIds.TARGET_FINISHED);
		waitIfSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void taskStarted(BuildEvent event) {
		super.taskStarted(event);
		fCurrentTask= event.getTask();
		
		marshalMessage(-1, DebugMessageIds.TASK_STARTED);
		waitIfSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void taskFinished(BuildEvent event) {
		super.taskFinished(event);
		fCurrentTask= null;
		marshalMessage(-1, DebugMessageIds.TASK_FINISHED);
		waitIfSuspended();
	}
	
	private void waitIfSuspended() {
		synchronized (this) {
			if (fCurrentTask != null) {
				String detail= null;
				boolean shouldSuspend= true;
				if (breakpointAtLineNumber(fCurrentTask.getLocation().getLineNumber())) {
					detail= DebugMessageIds.BREAKPOINT + ' ' + Integer.toString(fCurrentTask.getLocation().getLineNumber());
				} else if (fStepSuspend) {
					detail= DebugMessageIds.STEP;
					fStepSuspend= false;
				} else if (fClientSuspend) {
					detail= DebugMessageIds.CLIENT_REQUEST;
					fClientSuspend= false;
				} else {
					shouldSuspend= false;
				}
				if (shouldSuspend) {
					StringBuffer message= new StringBuffer(DebugMessageIds.SUSPENDED);
					message.append(detail);
					sendRequestResponse(message.toString());
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			} else if (fShouldSuspend) {
				try {
					fShouldSuspend= false;
					wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private boolean breakpointAtLineNumber(int lineNumber) {
		if (fBreakpoints == null) {
			return false;
		}
		for (int i = 0; i < fBreakpoints.length; i++) {
			int breakpointLineNumber = fBreakpoints[i];
			if (lineNumber == breakpointLineNumber) {
				return true;
			}
	
		}
		return false;
	}

	private void sendRequestResponse(String message) {
		if (fRequestWriter == null) {
			return;
		}
		
		fRequestWriter.println(message);
	}
	
	protected void marshallStack() {
		if (fCurrentTarget != null || fCurrentTask != null) {
			StringBuffer stackRepresentation= new StringBuffer();
			stackRepresentation.append(DebugMessageIds.STACK);
			stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
			stackRepresentation.append(fCurrentTarget.getName());
			if (fCurrentTask != null) {
				stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
				stackRepresentation.append(fCurrentTask.getTaskName());
			}
			stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
			stackRepresentation.append(fCurrentTask.getLocation().getFileName());
			stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
			stackRepresentation.append(fCurrentTask.getLocation().getLineNumber());
			sendRequestResponse(stackRepresentation.toString());
		}
	}
	
	protected void marshallProperties() {
		
	    StringBuffer propertiesRepresentation= new StringBuffer();
	    propertiesRepresentation.append(DebugMessageIds.PROPERTIES);
	    propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	    if (fCurrentTarget != null) {
	        Map currentProperties= fCurrentTarget.getProject().getProperties();
	        
	        if (fProperties != null && currentProperties.size() == fProperties.size()) {
	            //no new properties
	            sendRequestResponse(propertiesRepresentation.toString());
	            return;
	        }
	        
	        Iterator iter= currentProperties.keySet().iterator();
	        String propertyName;
	        String propertyValue;
	        while (iter.hasNext()) {
	            propertyName = (String) iter.next();
	            if (propertyName.equals("line.separator")) {
	            	continue;
	            }
	            if (fProperties == null || fProperties.get(propertyName) == null) { //new property
	                propertiesRepresentation.append(propertyName.length());
	                propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	                propertiesRepresentation.append(propertyName);
	                propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	                propertyValue= (String) currentProperties.get(propertyName);
	                propertiesRepresentation.append(propertyValue.length());
	                propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	                propertiesRepresentation.append(propertyValue);
	                propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	            }
	        }
	    }
	    sendRequestResponse(propertiesRepresentation.toString());
	}
	
	protected void addBreakpoint(String message) {
		int index= message.indexOf(' ');
		String lineNumber= message.substring(index + 1);
		if (fBreakpoints == null) {
			fBreakpoints= new int[]{Integer.parseInt(lineNumber)};
		} else {
			int[] temp= new int[fBreakpoints.length + 1];
			System.arraycopy(fBreakpoints, 0, temp, 0, fBreakpoints.length);
			temp[fBreakpoints.length]= Integer.parseInt(lineNumber);
			fBreakpoints= temp;
		}
	}
	
	protected void removeBreakpoint(String message) {
		if (fBreakpoints == null) {
			return;
		} 
		int index= message.indexOf(' ');
		String lineNumberString= message.substring(index + 1);
		int lineNumber= Integer.parseInt(lineNumberString);
		int i;
		for (i=0; i < fBreakpoints.length; i++) {
			if (fBreakpoints[i] == lineNumber) {
				break;
			}
		}
		if (fBreakpoints.length - 1 == 0) {
		   fBreakpoints= null;
		   return;
		}
		int[] temp= new int[fBreakpoints.length - 1];
		System.arraycopy(fBreakpoints, 0, temp, 0, i);
		System.arraycopy(fBreakpoints, i + 1, temp, i, fBreakpoints.length - 1);
		fBreakpoints= temp;
	}
}