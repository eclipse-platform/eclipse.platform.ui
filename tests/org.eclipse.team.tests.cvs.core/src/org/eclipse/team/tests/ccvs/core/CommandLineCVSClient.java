package org.eclipse.team.tests.ccvs.core;

import java.io.File;

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
		int returnCode = CVSTestSetup.executeCommand(commandLineBuf.toString(), null, localRoot);
		if (returnCode != 0) {
			throw new CVSClientException("Command line client returned non-zero code: " + returnCode);
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
}
