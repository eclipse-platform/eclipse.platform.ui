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

package org.eclipse.ant.tests.ui;

import org.eclipse.ant.tests.ui.testplugin.ConsoleLineTracker;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.model.IAntUIPreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.graphics.Color;


public class BuildTests extends AbstractAntUIBuildTest {

	public BuildTests(String name) {
		super(name);
	}
	
  /**
   * Tests launching Ant and getting messages
   * logged to the console.
   */
  public void testOutput() throws CoreException {
	  launch("echoing");
	  assertTrue("Incorrect number of messages logged for build. Should be 7. Was " + ConsoleLineTracker.getNumberOfMessages(), ConsoleLineTracker.getNumberOfMessages() == 7);
	  String message= ConsoleLineTracker.getMessage(6);
	  assertTrue("Incorrect last message. Should start with Total time:. Message: " + message, message.startsWith("Total time:"));
  }
  
  /**
	 * Tests launching Ant and that the
	 * correct links are in the console doc
	 */
	public void testLinks() throws CoreException {
		launch("echoing");
		int offset= 25; //buildfile link
		IConsoleHyperlink link= getHyperlink(offset, ConsoleLineTracker.getDocument());
		assertNotNull("No hyperlink found at offset " + offset, link);
		
		offset= 94; //echo link
		link= getHyperlink(offset, ConsoleLineTracker.getDocument());
		assertNotNull("No hyperlink found at offset " + offset, link);
	}
	
	/**
	 * Tests launching Ant and that the
	 * correct colors are in the console doc
	 */
	public void testColor() throws BadLocationException, CoreException {
		
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
		offset= 83; //echo
		color= getColorAtOffset(offset, ConsoleLineTracker.getDocument());
		assertNotNull("No color found at " + offset, color);
		assertEquals(color, AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_WARNING_RGB));
	}
}