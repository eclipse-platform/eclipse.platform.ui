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

import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.registry.NewWizardsRegistryReader;

/**
 * @since 3.1
 */
public class NewWizardTests extends DynamicTestCase {

	private static final String WIZARD_ID = "org.eclipse.newNewWizard1.newNewWizard1";
	
	/**
	 * 
	 */
	public NewWizardTests(String testName) {
		super(testName);
	}
	
	public void testNewWizardProperties() {
		NewWizardsRegistryReader reader = new NewWizardsRegistryReader();
		assertNull(reader.findWizard(WIZARD_ID));
		getBundle();
		reader = new NewWizardsRegistryReader();
		WorkbenchWizardElement wizard = reader.findWizard(WIZARD_ID);
		assertNotNull(wizard);
		testNewWizardProperties(wizard);
		removeBundle();
		reader = new NewWizardsRegistryReader();
		assertNull(reader.findWizard(WIZARD_ID));
		try {
			testNewWizardProperties(wizard);
			fail();
		}
		catch (RuntimeException e) {
		}
	}
	
	/**
	 * @param wizard
	 */
	private void testNewWizardProperties(WorkbenchWizardElement wizard) {
		assertNotNull(wizard.getID());
		assertNotNull(wizard.getDescription());
		assertNotNull(wizard.getHelpHref());
		assertNotNull(wizard.getDescriptionImage());
		assertNotNull(wizard.getImageDescriptor());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	protected String getExtensionId() {		
		return "newNewWizard1.testDynamicNewWizardAddition";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return IWorkbenchConstants.PL_NEW;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	protected String getInstallLocation() {
		return "data/org.eclipse.newNewWizard1";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getMarkerClass()
	 */
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicWizard";
	}
}
