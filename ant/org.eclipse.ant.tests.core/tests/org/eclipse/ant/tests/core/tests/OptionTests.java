package org.eclipse.ant.tests.core.tests;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.InputStream;

import org.eclipse.ant.tests.core.AbstractAntTest;
import org.eclipse.ant.tests.core.testplugin.AntTestChecker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class OptionTests extends AbstractAntTest {
	
	protected static final String START_OF_HELP= "ant [options] [target [target2 [target3] ...]]";
	protected static final String VERSION= "Ant version 1.5.1 compiled on October 2 2002";
	 
	public OptionTests(String name) {
		super(name);
	}
	
	/**
	 * Tests the "-help" option
	 */
	public void testHelp() throws CoreException {
		run("TestForEcho.xml", new String[]{"-help"});
		assertTrue("One message should have been logged", AntTestChecker.getDefault().getMessagesLoggedCount() == 1);
		assertTrue("Help is incorrect", getLastMessageLogged() != null && getLastMessageLogged().startsWith(START_OF_HELP));
		assertTrue("No build started should have occurred", AntTestChecker.getDefault().getBuildsStartedCount() == 0);
	}
	
	/**
	 * Tests the "-version" option
	 */
	public void testVersion() throws CoreException {
		run("TestForEcho.xml", new String[]{"-version"});
		assertTrue("One message should have been logged", AntTestChecker.getDefault().getMessagesLoggedCount() == 1);
		assertTrue("Version is incorrect", VERSION.equals(getLastMessageLogged()));
		assertTrue("No build started should have occurred", AntTestChecker.getDefault().getBuildsStartedCount() == 0);
	}
	
	/**
	 * Tests the "-projecthelp" option
	 */
	public void testProjecthelp() throws CoreException {
		run("TestForEcho.xml", new String[]{"-projecthelp", "-q"});
		assertTrue("One message should have been logged", AntTestChecker.getDefault().getMessagesLoggedCount() == 1);
		assertTrue("Project help is incorrect", VERSION.equals(getLastMessageLogged()));
	}
	
	
	/**
	 * Tests the "-logger" option with a logger that is not an instance of BuildLogger
	 */
	public void testBadLogger() throws CoreException {
		try {
			run("TestForEcho.xml", new String[]{"-logger", "java.lang.String"});
		} catch (CoreException ce) {
			return;
		}
		assertTrue("A core exception should have occurred wrappering a class cast exception", false);
	}
	
	/**
	 * Tests the "-listener" option with a listener that is not an instance of BuildListener
	 */
	public void testBadListener() throws CoreException {
		try {
			run("TestForEcho.xml", new String[]{"-listener", "java.lang.String"});
		} catch (CoreException ce) {
			return;
		}
		assertTrue("A core exception should have occurred wrappering a class cast exception", false);
	}
	
	/**
	 * Tests passing an unrecognized argument
	 */
	public void testUnknownArg() throws CoreException {
		
		run("TestForEcho.xml", new String[]{"-listenr"});
		//unknown arg, print usage
		assertTrue("Two message should have been logged", AntTestChecker.getDefault().getMessagesLoggedCount() == 2);
		assertTrue("Should have printed the usage", getLastMessageLogged() != null && getLastMessageLogged().startsWith(START_OF_HELP));
	}
	
	/**
	 * Tests specifying the -logfile with no arg
	 */
	public void testLogFileWithNoArg() throws CoreException {
		try {
			run("TestForEcho.xml", new String[]{"-logfile"});
		} catch (CoreException ce) {
			//You must specify a log file when using the -log argument
			return;
		}
		assertTrue("A core exception should have occurred as an unrecognized argument ", false);
	}
	
	/**
	 * Tests specifying the -logfile
	 */
	public void testLogFile() throws CoreException {
		run("TestForEcho.xml", new String[]{"-logfile", "TestLogFile.txt"});
		IFile file= checkFileExists("TestLogFile.txt");
	//	InputStream stream =file.getContents();
	//	stream.	
	}
	
	/**
	 * Tests specifying the -logger with no arg
	 */
	public void testLoggerWithNoArg() throws CoreException {
		try {
			run("TestForEcho.xml", new String[]{"-logger"});
		} catch (CoreException ce) {
			//You must specify a classname when using the -logger argument
			return;
		}
		assertTrue("A core exception should have occurred as an unrecognized argument ", false);
	}
	
	/**
	 * Tests specifying the -listener with no arg
	 */
	public void testListenerWithNoArg() throws CoreException {
		try {
			run("TestForEcho.xml", new String[]{"-listener"});
		} catch (CoreException ce) {
			
			return;
		}
		assertTrue("A core exception should have occurred as an unrecognized argument ", false);
	}
	
	/**
	 * Tests specifying the -listener with no arg
	 */
	public void testListener() throws CoreException {
		
		run("TestForEcho.xml", new String[]{"-listener", "TestBuildListener"});
		assertSuccessful();
		
	}
	
	/**
	 * Tests specifying the -buildfile with no arg
	 */
	public void testBuildFileWithNoArg() throws CoreException {
		try {
			run("TestForEcho.xml", new String[]{"-buildfile"});
		} catch (CoreException ce) {
			//You must specify a buildfile when using the -buildfile argument
			return;
		}
		assertTrue("A core exception should have occurred as an unrecognized argument ", false);
	}
	
	/**
	 * Tests specifying the -buildfile
	 */
	public void testBuildFile() throws CoreException {
		
		run("TestForEcho.xml", new String[]{"-buildfile", "scripts\\echoing.xml"});
		assertTrue("Should have been 3 tasks", AntTestChecker.getDefault().getTaskStartedCount() == 3);
		
	}
	
	/**
	 * Tests specifying a target at the command line
	 */
	public void testSpecifyBadTargetAsArg() throws CoreException {
		try {
			run("TestForEcho.xml", new String[]{"echo2"}, false);
		} catch (CoreException ce) {
			return;
		}
		assertTrue("A core exception should have occurred as the target does not exist", false);
	}
	
	/**
	 * Tests specifying a target at the command line
	 */
	public void testSpecifyTargetAsArg() throws CoreException {
		run("echoing.xml", new String[]{"echo3"}, false);
		assertTrue("3 messages should have been logged; was " + AntTestChecker.getDefault().getMessagesLoggedCount(), AntTestChecker.getDefault().getMessagesLoggedCount() == 3);
		assertSuccessful();
	}
	
	/**
	 * Tests specifying a target at the command line and quiet reporting
	 */
	public void testSpecifyTargetAsArgAndQuiet() throws CoreException {
		run("echoing.xml", new String[]{"-quiet", "echo3"}, false);
		assertTrue("1 message should have been logged; was " + AntTestChecker.getDefault().getMessagesLoggedCount(), AntTestChecker.getDefault().getMessagesLoggedCount() == 1);
	}
	
	/**
	 * Tests specifying a target at the command line and quiet reporting
	 */
	public void testMinusD() throws CoreException {
		run("echoing.xml", new String[]{"-DAntTests=testing", "-Declipse.is.cool=true"}, false);
		assertSuccessful();
		assertTrue("eclipse.is.cool should have been set as true", "true".equals(AntTestChecker.getDefault().getUserProperty("eclipse.is.cool")));
		assertTrue("AntTests should have a value of testing", "testing".equals(AntTestChecker.getDefault().getUserProperty("AntTests")));
		assertNull("my.name was not set and should be null", AntTestChecker.getDefault().getUserProperty("my.name"));
	}
}
