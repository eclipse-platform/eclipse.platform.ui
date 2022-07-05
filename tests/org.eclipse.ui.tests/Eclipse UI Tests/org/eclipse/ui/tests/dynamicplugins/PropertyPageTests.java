/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class PropertyPageTests extends DynamicTestCase {

	private static final String PROPERTYPAGE = "dynamicPropertyPage1";

	public PropertyPageTests() {
		super(PropertyPageTests.class.getSimpleName());
	}


	@Test
	public void testPropertyPageCount() {
		PropertyPageContributorManager manager = PropertyPageContributorManager.getManager();
		int size = manager.getContributors().size();
		getBundle();
		assertEquals(size + 1, manager.getContributors().size());
		removeBundle();
		assertEquals(size, manager.getContributors().size());
	}

	@Test
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

	@Override
	protected String getExtensionId() {
		return "newPropertyPage1.testDynamicPropertyPageAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_PROPERTY_PAGES;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newPropertyPage1";
	}

	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicPropertyPage";
	}
}
