package org.eclipse.team.tests.ccvs.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import junit.framework.Assert;

import org.eclipse.team.internal.ccvs.core.CVSException;

public class CommandLineCVSClient {
	private static final String cvsExecutable =
		System.getProperty("eclipse.cvs.command");

	public static void execute(
		String repositoryLocation, File localRoot, String command,
		String[] globalOptions, String[] localOptions,
		String[] arguments) throws CVSException {
		// test arguments
		Assert.assertNotNull(repositoryLocation);
		Assert.assertNotNull(localRoot);
		Assert.assertNotNull(command);
		Assert.assertNotNull(globalOptions);
		Assert.assertNotNull(localOptions);
		Assert.assertNotNull(arguments);
		Assert.assertTrue(localRoot.exists());

		// build command line
		StringBuffer commandLineBuf = new StringBuffer(cvsExecutable);
		commandLineBuf.append(" -d \"");
		commandLineBuf.append(repositoryLocation);
		commandLineBuf.append('"');
		appendStrings(commandLineBuf, globalOptions);
		commandLineBuf.append(' ');
		commandLineBuf.append(command);
		appendStrings(commandLineBuf, localOptions);
		appendStrings(commandLineBuf, arguments);
		
		// execute command
		try {
			PrintStream debugStream = CVSTestSetup.DEBUG ? System.out : null;
			String commandLine = commandLineBuf.toString();
			
			if (debugStream != null) {
				// while debugging, dump CVS command line client results to stdout
				// prefix distinguishes between message source stream
				debugStream.println();
				debugStream.println("CMD> " + commandLine);
				debugStream.println("DIR> " + localRoot.toString());
			}
			Process cvsProcess = Runtime.getRuntime().exec(commandLine, null, localRoot);
			// stream output must be dumped to avoid blocking the process or causing a deadlock
			startBackgroundPipeThread(cvsProcess.getErrorStream(), debugStream, "ERR> ");
			startBackgroundPipeThread(cvsProcess.getInputStream(), debugStream, "MSG> ");
			int returnCode = cvsProcess.waitFor();
			
			if (debugStream != null) {
				debugStream.println("RESULT> " + returnCode);
			}
			if (returnCode != 0) {
				throw new CVSClientException("Command line client returned non-zero code: " + returnCode);
			}
		} catch (IOException e) {
			throw new CVSClientException("IOException while executing command line client: " + e);
		} catch (InterruptedException e) {
			throw new CVSClientException("InterruptedException while executing command line client: " + e);
		}
	}
		
	private static void appendStrings(StringBuffer commandLine, String[] strings) {	
		for (int i = 0; i < strings.length; i++) {
			String string = strings[i];
			if (string != null && string.length() != 0) {
				commandLine.append(" \"");
				commandLine.append(string);
				commandLine.append('"');
			}
		}
	}
	
	private static void startBackgroundPipeThread(final InputStream is, final PrintStream os,
		final String prefix) {
		new Thread() {
			public void run() {
				BufferedReader reader = null;
				try {
					try {
						reader = new BufferedReader(new InputStreamReader(is));
						for (;;) {
							String line = reader.readLine();
							if (line == null) break;
							if (os != null) os.println(prefix + line);
						}
					} finally {
						if (reader != null) reader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
}
