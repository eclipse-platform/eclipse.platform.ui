/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.tests.ui.separateVM;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.ant.tests.ui.testplugin.ConsoleLineTracker;
import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.ant.ui.internal.model.IAntUIPreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class SeparateVMTests extends AbstractAntUITest {
	/**
	 * Flag that indicates test are in progress
	 */
	protected boolean testing = true;
		
    public SeparateVMTests(String name) {
        super(name);
    }
    
	public static Test suite() {
		return new TestSuite(SeparateVMTests.class);
	}

    /**
     * Tests launching Ant in a separate vm and getting messages
     * logged to the console.
     */
    public void testSeparateVM() throws CoreException {
      	launch("echoing");
      	assertTrue("Incorrect number of messages logged for build. Should be 6. Was " + ConsoleLineTracker.getNumberOfMessages(), ConsoleLineTracker.getNumberOfMessages() == 6);
      	assertTrue("Incorrect last message. Should start with Total time:. Message: " + ConsoleLineTracker.getMessage(4), ConsoleLineTracker.getMessage(4).startsWith("Total time:"));
    }
    
	/**
	 * Tests launching Ant in a separate vm and that the
	 * correct links are in the console doc
	 */
	public void testSeparateVMLinks() throws CoreException {
		launch("echoing");
		int offset= 15; //buildfile link
		IConsoleHyperlink link= getHyperlink(offset, ConsoleLineTracker.getDocument());
		assertNotNull("No hyperlink found at offset " + offset, link);
		
		offset= 87; //echo link
		link= getHyperlink(offset, ConsoleLineTracker.getDocument());
		assertNotNull("No hyperlink found at offset " + offset, link);
	}
	
	/**
	 * Tests launching Ant in a separate vm and that the
	 * correct colors are in the console doc
	 */
	public void testSeparateVMColor() throws BadLocationException, CoreException {
		
		//AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_INFO_RGB);	
		//AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_ERROR_RGB);
		//AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_DEBUG_RGB);
		//AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_VERBOSE_RGB);
		//AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_WARNING_RGB);
		
		launch("echoing");
		int offset= 15; //buildfile
		Color color= getColorAtOffset(offset, ConsoleLineTracker.getDocument());
		assertNotNull("No color found at " + offset, color);
		assertEquals(color, AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_INFO_RGB));
		offset= 87; //echo
		color= getColorAtOffset(offset, ConsoleLineTracker.getDocument());
		assertNotNull("No color found at " + offset, color);
		assertEquals(color, AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_WARNING_RGB));
	}
    
	/**
	 * Runs the test and collects the result in a TestResult without blocking
	 * the UI thread.
	 */
	public void run(final TestResult result) {
		final Display display = Display.getCurrent();
		Thread thread = null;
		try {
			Runnable r = new Runnable() {
				public void run() {
					SeparateVMTests.super.run(result);		
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