/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial implementation
 * 	   IBM Corporation - additional tests
 *******************************************************************************/

package org.eclipse.ant.tests.ui.testplugin;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ant.ui.internal.editor.test.AntEditorContentOutlineTests;
import org.eclipse.ant.ui.internal.editor.test.CodeCompletionTest;
import org.eclipse.ant.ui.internal.editor.test.EnclosingTargetSearchingHandlerTest;
import org.eclipse.ant.ui.internal.editor.test.TaskDescriptionProviderTest;

/**
 * Test suite for the Ant UI
 * 
 */
public class AntUITests extends TestSuite {
	
	/**
		 * Flag that indicates test are in progress
		 */
		protected boolean testing = true;

    public static Test suite() {

        TestSuite suite= new AntUITests();
        suite.setName("Ant UI Unit Tests");
		suite.addTest(new TestSuite(ProjectCreationDecorator.class));
		//suite.addTest(new TestSuite(SeparateVMTests.class));
        suite.addTest(new TestSuite(CodeCompletionTest.class));
        suite.addTest(new TestSuite(TaskDescriptionProviderTest.class));
        suite.addTest(new TestSuite(AntEditorContentOutlineTests.class));
        suite.addTest(new TestSuite(EnclosingTargetSearchingHandlerTest.class));
        return suite;
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
//						if (result.shouldStop() )
//							break;
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
