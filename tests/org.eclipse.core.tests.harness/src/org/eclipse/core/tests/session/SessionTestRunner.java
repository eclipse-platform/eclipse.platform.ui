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
import junit.framework.TestResult;
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
			Socket connection = null;
			try {
				// someone asked us to stop before we could do anything
				if (!shouldRun())
					return;
				try {
					connection = serverSocket.accept();
				} catch (SocketException se) {
					if (!shouldRun())
						// we have been finished without ever getting any connections
						// no need to throw exception
						return;
					// something else stopped us
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
							this.wait(150);
						}
					}
				} catch (InterruptedException e) {
					// not expected
				}
			} catch (IOException e) {
				EclipseWorkspaceTest.log(e);
			} finally {
				// remember we are already finished
				markAsFinished();
				// cleanup
				try {
					if (connection != null && !connection.isClosed())
						connection.close();
				} catch (IOException e) {
					EclipseWorkspaceTest.log(e);
				}
				try {
					if (serverSocket != null && !serverSocket.isClosed())
						serverSocket.close();
				} catch (IOException e) {
					EclipseWorkspaceTest.log(e);
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
					EclipseWorkspaceTest.log(e);
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

	/**
	 * 	Creates a brand new setup object to be used for this session only, based 
	 * on the setup provided by the test descriptor.
	 * 
	 * @param descriptor a test descriptor for the session test to run
	 * @param port the port used by the result collector 
	 * @return a brand new setup
	 */
	private Setup createSetup(TestDescriptor descriptor, int port) {
		Setup setup = (Setup) descriptor.getSetup().clone();
		setup.setApplication(descriptor.getApplicationId());
		StringBuffer eclipseArgs = new StringBuffer(200);
		if (setup.getEclipseArgs() != null)
			eclipseArgs.append(setup.getEclipseArgs());
		eclipseArgs.append(" -testpluginname ");
		eclipseArgs.append(descriptor.getPluginId());
		eclipseArgs.append(" -test ");
		eclipseArgs.append(descriptor.getTestClass());
		eclipseArgs.append(':');
		eclipseArgs.append(descriptor.getTestMethod());
		eclipseArgs.append(" -port ");
		eclipseArgs.append(port);
		setup.setEclipseArgs(eclipseArgs.toString());
		return setup;
	}

	/**
	 * Runs the setup. Returns a status object indicating the outcome of the operation.  
	 * @param timeout
	 * @return a status object indicating the outcome 
	 */
	private IStatus launch(String command, long timeout) {
		if (Platform.inDebugMode()) {
			System.out.println("Command line: ");
			System.out.print('\t');
			System.out.println(command);
		}
		IStatus outcome = Status.OK_STATUS;
		try {
			ProcessController process = new ProcessController(timeout, command);
			process.forwardErrorOutput(System.err);
			process.forwardOutput(System.out);
			//if necessary to interact with the spawned process, this would have
			// to be done
			//process.forwardInput(System.in);
			int returnCode = process.execute();
			if (returnCode != 0)
				outcome = new Status(IStatus.WARNING, Platform.PI_RUNTIME, returnCode, "Process returned non-zero code: " + returnCode + "\n\tCommand: " +command, null);
		} catch (Exception e) {
			outcome = new Status(IStatus.ERROR, Platform.PI_RUNTIME, -1, "Error running process\n\tCommand: " +command, e);
		}
		return outcome;
	}

	/**
	 * Runsthe test described  in a separate session using 
	 * @param descriptor
	 * @param result
	 * @param sessionSetup
	 */
	public final void run(TestDescriptor descriptor, TestResult result) {
		result.startTest(descriptor.getTest());
		try {
			ResultCollector collector = null;
			try {
				collector = new ResultCollector();
			} catch (IOException e) {
				result.addError(descriptor.getTest(), e);
				return;
			}
			Setup setup = createSetup(descriptor, collector.getPort());
			new Thread(collector, "Test result collector").start();
			IStatus status = launch(setup.getCommandLine(), setup.getTimeout());
			collector.shutdown();
			// ensure the session ran without any errors
			if (!status.isOK()) {
				EclipseWorkspaceTest.log(status);
				if (status.getSeverity() == IStatus.ERROR) {
					result.addError(descriptor.getTest(), new CoreException(status));
					return;
				}
			}
			Result collected = collector.getResult();
			if (collected == null) {
				if (!descriptor.isCrashTest())
					result.addError(descriptor.getTest(), new Exception("Test did not run"));
			} else if (collected.type == Result.FAILURE)
				result.addFailure(descriptor.getTest(), new RemoteAssertionFailedError(collected.message, collected.stackTrace));
			else if (collected.type == Result.ERROR)
				result.addError(descriptor.getTest(), new RemoteTestException(collected.message, collected.stackTrace));
			else if (descriptor.isCrashTest())
				result.addError(descriptor.getTest(), new Exception("Crash test failed to cause crash"));
		} finally {
			result.endTest(descriptor.getTest());
		}
	}
}