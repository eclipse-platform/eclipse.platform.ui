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
import junit.framework.TestSuite;

import org.eclipse.ant.tests.ui.AbstractAntUIBuildTest;
import org.eclipse.ant.tests.ui.testplugin.ConsoleLineTracker;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.model.IAntUIPreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.graphics.Color;

public class SeparateVMTests extends AbstractAntUIBuildTest {
		
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
      	launch("echoingSepVM");
      	assertTrue("Incorrect number of messages logged for build. Should be 5. Was " + ConsoleLineTracker.getNumberOfMessages(), ConsoleLineTracker.getNumberOfMessages() == 5);
      	assertTrue("Incorrect last message. Should start with Total time:. Message: " + ConsoleLineTracker.getMessage(4), ConsoleLineTracker.getMessage(4).startsWith("Total time:"));
    }
    
	/**
	 * Tests launching Ant in a separate vm and that the
	 * correct links are in the console doc
	 */
	public void testSeparateVMLinks() throws CoreException {
		launch("echoingSepVM");
		int offset= 15; //buildfile link
		IConsoleHyperlink link= getHyperlink(offset, ConsoleLineTracker.getDocument());
		assertNotNull("No hyperlink found at offset " + offset, link);
		try {
			offset= ConsoleLineTracker.getDocument().getLineOffset(2) + 10; //echo link
		} catch (BadLocationException e) {
			assertTrue("failed getting offset of line", false);
		}
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
		
		launch("echoingSepVM");
		int offset= 15; //buildfile
		Color color= getColorAtOffset(offset, ConsoleLineTracker.getDocument());
		assertNotNull("No color found at " + offset, color);
		assertEquals(color, AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_INFO_RGB));
		try {
			offset= ConsoleLineTracker.getDocument().getLineOffset(2) + 10; //echo link
		} catch (BadLocationException e) {
			assertTrue("failed getting offset of line", false);
		}
		color= getColorAtOffset(offset, ConsoleLineTracker.getDocument());
		assertNotNull("No color found at " + offset, color);
		assertEquals(color, AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_WARNING_RGB));
	}
}