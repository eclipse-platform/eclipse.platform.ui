package org.eclipse.ant.tests.core.tests;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.ant.tests.core.AbstractAntTest;
import org.eclipse.ant.tests.core.testplugin.AntLoggerChecker;
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
		assertTrue("One message should have been logged", AntLoggerChecker.getDefault().getMessagesLoggedCount() == 1);
		assertTrue("Help is incorrect", AntLoggerChecker.getDefault().getLastMessageLogged() != null && AntLoggerChecker.getDefault().getLastMessageLogged().startsWith(START_OF_HELP));
	}
	
	/**
	 * Tests the "-version" option
	 */
	public void testVersion() throws CoreException {
		run("TestForEcho.xml", new String[]{"-version"});
		assertTrue("One message should have been logged", AntLoggerChecker.getDefault().getMessagesLoggedCount() == 1);
		assertTrue("Version is incorrect", VERSION.equals(AntLoggerChecker.getDefault().getLastMessageLogged()));
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
}
