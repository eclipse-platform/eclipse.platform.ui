/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.TestPlugin;

public class Perspectives extends TestCase {
	private IPerspectiveRegistry fReg;
	
	public Perspectives(String testName) {
		super(testName);
	}
	public void setUp() {
	}
	public void installPlugin(String pluginName) {
		// Programmatically install a new plugin
		TestPlugin plugin = TestPlugin.getDefault();
		assertNotNull(plugin);
		String pluginLocation = null;
		try {
			URL dataURL = Platform.resolve(plugin.getBundle().getEntry(pluginName));
			pluginLocation = "reference:" + dataURL.toExternalForm();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		TestInstallUtil install = new TestInstallUtil(pluginLocation);
		install.installBundle();
	}

	public void testFindPerspectiveWithId() {
		// Just try to find the new perspective.  Don't actually try to
		// do anything with it as the class it refers to does not exist.
		synchronized(this) {
			try {
				installPlugin("data/org.eclipse.newPerspective1");
				wait(1000l);
				fReg = PlatformUI.getWorkbench().getPerspectiveRegistry();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		IPerspectiveDescriptor found = fReg.findPerspectiveWithId("org.eclipse.newPerspective1.newPerspective1");
		assertNotNull(found);
	}
}
