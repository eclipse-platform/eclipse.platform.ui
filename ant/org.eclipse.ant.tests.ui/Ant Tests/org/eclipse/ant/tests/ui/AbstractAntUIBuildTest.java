/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.tests.ui;

import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.ant.tests.ui.testplugin.ConsoleLineTracker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IHyperlink;


public abstract class AbstractAntUIBuildTest extends AbstractAntUITest {
		
	public AbstractAntUIBuildTest(String name) {
		super(name);
	}
		
	/**
	 * Launches the Ant build with the buildfile name (no extension).
	 * Waits for all of the lines to be appended to the console.
	 * 
	 * @param buildFileName the buildfile to execute
	 * @return thread in which the first suspend event occurred
	 */
	protected void launch(String buildFileName) throws CoreException {
		super.launch(buildFileName);
		ConsoleLineTracker.waitForConsole();
	}
	
	/**
	 * Launches the launch configuration
	 * Waits for all of the lines to be appended to the console.
	 * 
	 * @param config the config to execute
	 * @return thread in which the first suspend event occurred
	 */
	protected void launch(ILaunchConfiguration config) throws CoreException {
	    launchAndTerminate(config, 20000);
		ConsoleLineTracker.waitForConsole();
	}
	
	protected void activateLink(final IHyperlink link) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				link.linkActivated();
			}
		});
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest#launch(java.lang.String, java.lang.String)
	 */
	protected void launch(String buildFileName, String arguments) throws CoreException {
		super.launch(buildFileName, arguments);
		ConsoleLineTracker.waitForConsole();
	}
}
