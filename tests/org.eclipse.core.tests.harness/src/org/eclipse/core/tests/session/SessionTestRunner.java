/******************************************************************************* 
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.session;

import java.io.*;
import java.net.*;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class SessionTestRunner {

	class Result {
		final static int ERROR = 2;
		final static int FAILURE = 1;
		final static int SUCCESS = 0;
		String message;
		String stackTrace;
		int type;
	}

	/**
	 * Collectors can be used a single time only.
	 */
	class ResultCollector implements Runnable {
		private boolean finished;
		private Result newResult = new Result();
		private Result result;
		ServerSocket serverSocket;
		private boolean shouldRun = true;
		private StringBuffer stack;

		ResultCollector() throws IOException {
			serverSocket = new ServerSocket(0);
		}

		public int getPort() {

			return serverSocket.getLocalPort();
		}

		public Result getResult() {
			return result;
		}

		public synchronized boolean isFinished() {
			return finished;
		}

		private synchronized void markAsFinished() {
			finished = true;
			notifyAll();
		}

		private void processAvailableMessages(BufferedReader messageReader) throws IOException {
			while (messageReader.ready()) {
				String message = messageReader.readLine();
				processMessage(message);
			}
		}

		private void processMessage(String message) {
			if (message.startsWith("%ERROR")) {
				newResult.type = Result.ERROR;
				newResult.message = "";
				return;
			}
			if (message.startsWith("%FAILED")) {
				newResult.type = Result.FAILURE;
				newResult.message = "";
				return;
			}
			if (message.startsWith("%TRACES")) {
				stack = new StringBuffer();
				return;
			}
			if (message.startsWith("%TRACEE")) {
				newResult.stackTrace = stack.toString();
				stack = null;
				return;
			}
			if (stack != null) {
				stack.append(message);
				stack.append(System.getProperty("line.separator"));
				return;
			}
			if (message.startsWith("%RUNTIME")) {
				result = newResult;
				return;
			}
		}

		public void run() {
			if (!shouldRun())
				return;
			Socket connection = null;
			try {
				try {
					connection = serverSocket.accept();
				} catch (SocketException se) {
					if (!shouldRun())
						// we have been finished without ever getting any connections
						return;
					throw se;
				}
				BufferedReader messageReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
				try {
					// main loop
					while (true) {
						synchronized (this) {
							processAvailableMessages(messageReader);
							if (!shouldRun())
								return;
							this.wait(500);
						}
					}
				} catch (InterruptedException e) {
					// not expected
				}
			} catch (IOException e) {
				log(e);
			} finally {
				// remember we are already finished
				markAsFinished();
				// cleanup
				try {
					if (connection != null && !connection.isClosed())
						connection.close();
				} catch (IOException e) {
					log(e);
				}
				try {
					serverSocket.close();
				} catch (IOException e) {
					log(e);
				}
			}
		}

		private synchronized boolean shouldRun() {
			return shouldRun;
		}

		/*
		 * Politely asks the collector thread to stop and wait until it is finished.
		 */
		public void shutdown() {
			// ask the collector to stop
			synchronized (this) {
				if (isFinished())
					return;
				shouldRun = false;
				try {
					serverSocket.close();
				} catch (IOException e) {
					log(e);
				}
				notifyAll();
			}
			// wait until the collector is done
			synchronized (this) {
				while (!isFinished())
					try {
						wait(100);
					} catch (InterruptedException e) {
						// we don't care
					}
			}
		}

	}

	private String applicationId;
	private Setup baseSetup;
	private String pluginId;

	public static void log(IStatus status) {
		InternalPlatform.getDefault().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, EclipseWorkspaceTest.PI_HARNESS, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}

	public SessionTestRunner(String pluginId, String applicationId) {
		this.pluginId = pluginId;
		this.applicationId = applicationId;
	}

	private Setup createSetup(TestCase test, Setup sessionSetup, int port) {
		Setup setup = (Setup) (sessionSetup != null ? sessionSetup.clone() : getBaseSetup().clone());
		setup.setApplication(applicationId);
		StringBuffer appArgs = new StringBuffer(setup.getApplicationArgs());
		appArgs.append(" -testpluginname ");
		appArgs.append(pluginId);
		appArgs.append(" -test ");
		appArgs.append(test.getClass().getName());
		appArgs.append(':');
		appArgs.append(test.getName());
		appArgs.append(" -port ");
		appArgs.append(port);
		setup.setApplicationArgs(appArgs.toString());
		return setup;
	}

	public Setup getBaseSetup() {
		if (baseSetup == null)
			return SetupManager.getInstance().getDefaultSetup();
		return baseSetup;
	}

	/**
	 * Runs the setup. Returns a status object indicating the outcome of the operation.  
	 * @param timeout
	 * @return a status object indicating the outcome 
	 */
	private IStatus launch(String command, long timeout) {
		IStatus outcome = Status.OK_STATUS;
		try {
			ProcessController process = new ProcessController(timeout, command);
			process.forwardErrorOutput(System.err);
			process.forwardOutput(System.out);
			int returnCode = process.execute();
			if (returnCode != 0)
				outcome = new Status(IStatus.WARNING, Platform.PI_RUNTIME, returnCode, "Process returned non-zero code: " + returnCode, null);
		} catch (Exception e) {
			outcome = new Status(IStatus.ERROR, Platform.PI_RUNTIME, -1, "Error running process", e);
		}
		return outcome;
	}

	public void run(TestCase test, TestResult result, Setup sessionSetup) {
		result.startTest(test);
		try {
			ResultCollector collector = null;
			try {
				collector = new ResultCollector();
			} catch (IOException e) {
				result.addError(test, e);
				return;
			}
			Setup setup = createSetup(test, sessionSetup, collector.getPort());
			new Thread(collector).start();
			result.startTest(test);
			IStatus status = launch(setup.getCommandLine(), setup.getTimeout());
			collector.shutdown();
			// ensure the session ran without any errors
			if (!status.isOK()) {
				log(status);
				result.addError(test, new CoreException(status));
				return;
			}
			Result collected = collector.getResult();
			if (collected == null)
				// should never happen
				result.addError(test, new Exception("Test did not run"));
			else if (collected.type == Result.FAILURE)
				result.addFailure(test, new RemoteAssertionFailedError(collected.message, collected.stackTrace));
			else if (collected.type == Result.ERROR)
				result.addError(test, new RemoteTestException(collected.message, collected.stackTrace));
		} finally {
			result.endTest(test);
		}
	}

	public void setBaseSetup(Setup baseSetup) {
		this.baseSetup = baseSetup;
	}
}