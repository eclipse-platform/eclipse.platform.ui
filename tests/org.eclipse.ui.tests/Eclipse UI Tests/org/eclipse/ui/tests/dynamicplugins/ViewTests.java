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

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IViewDescriptor;
import org.eclipse.ui.internal.registry.IViewRegistry;
import junit.framework.TestCase;

/**
 * Tests to ensure the addition of new views with dynamic plug-ins.
 */

public class ViewTests extends TestCase {
	private IViewRegistry fReg;
		
	public ViewTests(String testName) {
		super(testName);
	}

	public void testFindViewInRegistry() {
		// Just try to find the new view.  Don't actually try to
		// do anything with it as the class it refers to does not exist.
		synchronized(this) {
			assertTrue(DynamicUtils.installPlugin("data/org.eclipse.newView1"));
			try {
				wait(1000l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			fReg = WorkbenchPlugin.getDefault().getViewRegistry();
		}
		IViewDescriptor found = fReg.find("org.eclipse.newView1.newView1");
		assertNotNull(found);
	}
}
