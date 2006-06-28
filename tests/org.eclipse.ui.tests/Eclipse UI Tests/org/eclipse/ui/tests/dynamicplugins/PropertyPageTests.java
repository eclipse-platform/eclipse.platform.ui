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

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.1
 */
public class PropertyPageTests extends DynamicTestCase {

	private static final String PROPERTYPAGE = "dynamicPropertyPage1";
	/**
	 * @param testName
	 */
	public PropertyPageTests(String testName) {
		super(testName);
	}


	public void testPropertyPageCount() {
		PropertyPageContributorManager manager = PropertyPageContributorManager.getManager();
		int size = manager.getContributors().size();
		getBundle();
		assertEquals(size + 1, manager.getContributors().size());
		removeBundle();
		assertEquals(size, manager.getContributors().size());
	}
	
	public void testPropertyPageContribution() {
		PropertyPageContributorManager cManager = PropertyPageContributorManager.getManager();
		PropertyPageManager manager; 
		DynamicTestType type = new DynamicTestType();
			
		cManager.contribute(manager = new PropertyPageManager(), type);
		assertNull(manager.find(PROPERTYPAGE));
		getBundle();
		cManager.contribute(manager = new PropertyPageManager(), type);
		IPreferenceNode result = manager.find(PROPERTYPAGE);
		assertNotNull(result);
		result.createPage(); // muck around and ensure we've created some potential garbage
		result.disposeResources();
		removeBundle();
		cManager.contribute(manager = new PropertyPageManager(), type);
		assertNull(manager.find(PROPERTYPAGE));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	protected String getExtensionId() {
		return "newPropertyPage1.testDynamicPropertyPageAddition";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_PROPERTY_PAGES;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	protected String getInstallLocation() {
		return "data/org.eclipse.newPropertyPage1";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getMarkerClass()
	 */
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicPropertyPage";
	}
}
