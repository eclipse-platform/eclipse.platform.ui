/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.core;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ant.tests.core.tests.FrameworkTests;
import org.eclipse.ant.tests.core.tests.OptionTests;
import org.eclipse.ant.tests.core.tests.ProjectTests;
import org.eclipse.ant.tests.core.tests.TargetTests;
import org.eclipse.ant.tests.core.tests.TaskTests;
import org.eclipse.ant.tests.core.tests.TypeTests;

/**
 * Test all areas of Ant.
 * 
 * To run this test suite:
 * <ol>
 * <li>Create a new JUnit plugin test launch configuration</li>
 * <li>Set the Test class to "org.eclipse.ant.tests.core.AutomatedSuite"</li>
 * <li>Set the Project to "org.eclipse.ant.tests.core"</li>
 * <li>Run the launch configuration. Output from the tests will be displayed in a JUnit view</li>
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
		addTest(new TestSuite(TypeTests.class));
	}

	/**
	 * Runs the tests and collects their result in a TestResult without blocking.
	 * the UI thread. Not normally needed but nice to have to check on the state of
	 * environment during a test run.
	 */
//	public void run(final TestResult result) {
//		final Display display = Display.getCurrent();
//		Thread thread = null;
//		try {
//			Runnable r = new Runnable() {
//				public void run() {
//					for (Enumeration e= tests(); e.hasMoreElements(); ) {
//				  		if (result.shouldStop() )
//				  			break;
//						Test test= (Test)e.nextElement();
//						runTest(test, result);
//					}					
//					testing = false;
//					display.wake();
//				}
//			};
//			thread = new Thread(r);
//			thread.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//				
//		while (testing) {
//			try {
//				if (!display.readAndDispatch())
//					display.sleep();
//			} catch (Throwable e) {
//				e.printStackTrace();
//			}			
//		}		
//	}
}

