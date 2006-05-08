/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.1
 */
public class HelpSupportTests extends DynamicTestCase {

	/**
	 * @param testName
	 */
	public HelpSupportTests(String testName) {
		super(testName);
	}

	public void testHelpSupport() {
		WorkbenchHelpSystem help = WorkbenchHelpSystem.getInstance();
		help.setDesiredHelpSystemId(getExtensionId());
		assertFalse(help.hasHelpUI());
		
		getBundle();
		help.dispose();
		assertTrue(help.hasHelpUI());
		
		removeBundle();
		help.dispose();		
		assertFalse(help.hasHelpUI());
		
		help.setDesiredHelpSystemId(null);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	protected String getExtensionId() {
		return "newHelpSupport1.testDynamicHelpSupportAddition";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_HELPSUPPORT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	protected String getInstallLocation() {
		return "data/org.eclipse.newHelpSupport1";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getMarkerClass()
	 */
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicHelpSupport";
	}

}
