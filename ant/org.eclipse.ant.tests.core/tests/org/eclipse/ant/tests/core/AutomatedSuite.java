package org.eclipse.ant.tests.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.Enumeration;

import junit.framework.*;

import org.eclipse.ant.tests.core.tests.*;
import org.eclipse.swt.widgets.Display;

/**
 * Test all areas of Ant.
 * 
 * To run this test suite:
 * <ol>
 * <li>Create a new Run-time Workbench launch configuration</li>
 * <li>Append "org.eclipse.ant.tests.core.AutomatedSuite" to the Program Arguments</li>
 * <li>Set the Application Name to "org.eclipse.ant.tests.core.app"</li>
 * <li>Run the launch configuration. Output from the tests will be written to the debug console</li>
 * </ol>
 */
public class AutomatedSuite extends TestSuite {
	
	/**
	 * Flag that indicates test are in progress
	 */
	protected boolean testing = true;

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new AutomatedSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public AutomatedSuite() {
		addTest(new TestSuite(ProjectCreationDecorator.class));
		addTest(new TestSuite(FrameworkTests.class));
		addTest(new TestSuite(TargetTests.class));
		addTest(new TestSuite(ProjectTests.class));
		addTest(new TestSuite(OptionTests.class));
		addTest(new TestSuite(TaskTests.class));
		//addTest(new TestSuite(TypeTests.class));
	}

	/**
	 * Runs the tests and collects their result in a TestResult.
	 * The Ant tests cannot be run in the UI thread or the event
	 * waiter blocks the UI when a resource changes.
	 */
	public void run(final TestResult result) {
		final Display display = Display.getCurrent();
		Thread thread = null;
		try {
			Runnable r = new Runnable() {
				public void run() {
					for (Enumeration e= tests(); e.hasMoreElements(); ) {
				  		if (result.shouldStop() )
				  			break;
						Test test= (Test)e.nextElement();
						runTest(test, result);
					}					
					testing = false;
					display.wake();
				}
			};
			thread = new Thread(r);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		while (testing) {
			try {
				if (!display.readAndDispatch())
					display.sleep();
			} catch (Throwable e) {
				e.printStackTrace();
			}			
		}		
	}
}

