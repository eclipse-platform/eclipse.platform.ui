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

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;
import junit.framework.TestCase;

/**
 * Tests to check the addition of a new perspective once the perspective
 * registry is loaded.
 */

public class PerspectiveTests extends TestCase /* implements IRegistryChangeListener */ {
	private IPerspectiveRegistry fReg;
//	private static boolean perspectiveRegistryUpdated = false;
	
	public PerspectiveTests(String testName) {
		super(testName);
//		Platform.getExtensionRegistry().addRegistryChangeListener(this);
	}

//	/* (non-Javadoc)
//	 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
//	 */
//	public void registryChanged(IRegistryChangeEvent event) {
//		// Just retrieve any changes relating to the extension point
//		// org.eclipse.ui.perspectives
//		IExtensionDelta delta[] = event.getExtensionDeltas(WorkbenchPlugin.PI_WORKBENCH, IWorkbenchConstants.PL_PERSPECTIVES);
//		perspectiveRegistryUpdated = true;
//	}
//
	public void testFindPerspectiveInRegistry() {
		// Just try to find the new perspective.  Don't actually try to
		// do anything with it as the class it refers to does not exist.
		synchronized(this) {
//			perspectiveRegistryUpdated = false;
			assertTrue(DynamicUtils.installPlugin("data/org.eclipse.newPerspective1"));
			try {
				wait(1000l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//			int i = 0;
//			while (!perspectiveRegistryUpdated && i < 1000) {
//				i++;
//			}
			fReg = PlatformUI.getWorkbench().getPerspectiveRegistry();
		}
		IPerspectiveDescriptor found = fReg.findPerspectiveWithId("org.eclipse.newPerspective1.newPerspective1");
		assertNotNull(found);
	}
}
